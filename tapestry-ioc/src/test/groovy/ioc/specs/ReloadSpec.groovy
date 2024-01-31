package ioc.specs

import org.apache.tapestry5.internal.plastic.PlasticInternalUtils
import org.apache.tapestry5.internal.plastic.asm.ClassWriter
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.ioc.services.UpdateListenerHub

import spock.lang.AutoCleanup
import spock.lang.Specification
import com.example.*

import static org.apache.tapestry5.internal.plastic.asm.Opcodes.*

class ReloadSpec extends Specification {

  private static final String PACKAGE = "com.example";

  private static final String CLASS = PACKAGE + ".ReloadableServiceImpl";

  private static final String BASE_CLASS = PACKAGE + ".BaseReloadableServiceImpl";


  @AutoCleanup("shutdown")
  Registry registry

  @AutoCleanup("deleteDir")
  File classesDir

  ClassLoader classLoader

  File classFile;

  def createRegistry() {
    registry = new RegistryBuilder(classLoader).add(ReloadModule).build()
  }

  /** Any unrecognized methods are evaluated against the registry. */
  def methodMissing(String name, args) {
    registry."$name"(* args)
  }


  def setup() {
    def uid = UUID.randomUUID().toString()

    classesDir = new File(System.getProperty("java.io.tmpdir"), uid)

    def classesURL = new URL(classesDir.toURI().toString() + "/")    

    classLoader = new URLClassLoader([classesURL] as URL[],
        Thread.currentThread().contextClassLoader)

    classFile = new File(classesDir, PlasticInternalUtils.toClassPath(CLASS))
  }

  def createImplementationClass(String status) {
    createImplementationClass CLASS, status
  }

  def createImplementationClass(String className, String status) {

    String internalName = PlasticInternalUtils.toInternalName className

    createClassWriter(internalName, "java/lang/Object", ACC_PUBLIC).with {

      // Add default constructor

      visitMethod(ACC_PUBLIC, "<init>", "()V", null, null).with {
        visitCode()
        visitVarInsn ALOAD, 0
        visitMethodInsn INVOKESPECIAL, "java/lang/Object", "<init>", "()V"
        visitInsn RETURN
        visitMaxs 1, 1
        visitEnd()
      }


      visitMethod(ACC_PUBLIC, "getStatus", "()Ljava/lang/String;", null, null).with {
        visitCode()
        visitLdcInsn status
        visitInsn ARETURN
        visitMaxs 1, 1
        visitEnd()
      }

      visitEnd()

      writeBytecode it, internalName
    }

  }

  def createClassWriter(String internalName, String baseClassInternalName, int classModifiers) {
    ClassWriter cw = new ClassWriter(0);

    cw.visit V1_5, classModifiers, internalName, null,
        baseClassInternalName, [
            PlasticInternalUtils.toInternalName(ReloadableService.name)
        ] as String[]


    return cw
  }


  def writeBytecode(ClassWriter cw, String internalName) {
    byte[] bytecode = cw.toByteArray();

    writeBytecode(bytecode, pathForInternalName(internalName))
  }

  def writeBytecode(byte[] bytecode, String path) {
    File file = new File(path)

    file.parentFile.mkdirs()

    file.withOutputStream { it.write bytecode }
  }


  def pathForInternalName(String internalName) {
    return String.format("%s/%s.class",
        classesDir.getAbsolutePath(),
        internalName)
  }

  def update() {
    getService(UpdateListenerHub).fireCheckForUpdates()
  }

  def "reload a service implementation"() {

    when:

    createImplementationClass "initial"

    createRegistry()

    ReloadableService reloadable = getService(ReloadableService);

    update()

    then:

    reloadable.status == "initial"

    when:

    update()

    touch classFile

    createImplementationClass "updated"

    then:

    // Changes do not take effect until after update check

    reloadable.status == "initial"

    when:

    update()

    then:

    reloadable.status == "updated"
  }

  def "reload a base class"() {

    setup:

    def baseClassInternalName = PlasticInternalUtils.toInternalName BASE_CLASS
    def internalName = PlasticInternalUtils.toInternalName CLASS

    createImplementationClass BASE_CLASS, "initial from base"

    createClassWriter(internalName, baseClassInternalName, ACC_PUBLIC).with {

      visitMethod(ACC_PUBLIC, "<init>", "()V", null, null).with {
        visitCode()
        visitVarInsn ALOAD, 0
        visitMethodInsn INVOKESPECIAL, baseClassInternalName, "<init>", "()V"
        visitInsn RETURN
        visitMaxs 1, 1
        visitEnd()
      }

      visitEnd()

      writeBytecode it, internalName
    }

    createRegistry()

    when:

    ReloadableService reloadable = getService(ReloadableService)

    update()

    then:

    reloadable.status == "initial from base"

    when:

    touch(new File(pathForInternalName(baseClassInternalName)))

    createImplementationClass BASE_CLASS, "updated from base"

    update()

    then:

    reloadable.status == "updated from base"
  }

  def "deleting an implementation class results in a runtime exception when reloading"() {

    when:

    createImplementationClass "before delete"

    createRegistry()

    ReloadableService reloadable = getService ReloadableService

    then:

    reloadable.status == "before delete"

    assert classFile.exists()

    when:

    classFile.delete()

    update()

    reloadable.getStatus()

    then:

    RuntimeException e = thrown()

    e.message.contains "Unable to reload class $CLASS"
  }


  def "reload a proxy object"() {
    when:

    createImplementationClass "initial proxy"

    createRegistry()

    def clazz = classLoader.loadClass CLASS

    ReloadableService reloadable = proxy(ReloadableService, clazz)

    then:

    reloadable.status == "initial proxy"

    when:

    touch classFile

    createImplementationClass "updated proxy"

    update()

    then:

    reloadable.status == "updated proxy"

    when:

    touch classFile

    createImplementationClass "re-updated proxy"

    update()

    then:

    reloadable.status == "re-updated proxy"
  }

  def "check exception message for invalid service implementation (lacking a public constructor)"() {

    when:

    createImplementationClass "initial"

    createRegistry()

    ReloadableService reloadable = getService ReloadableService

    touch classFile

    createInvalidImplementationClass()

    update()

    reloadable.getStatus()

    then:

    Exception e = thrown()

    e.message == "Service implementation class com.example.ReloadableServiceImpl does not have a suitable public constructor."
  }

  def "ensure ReloadAware services are notified when services are reloaded"() {

    when:

    registry = new RegistryBuilder().add(ReloadAwareModule).build()

    then:

    ReloadAwareModule.counterInstantiations == 0
    ReloadAwareModule.counterReloads == 0

    when:

    Counter counter = proxy(Counter, CounterImpl)

    then:

    ReloadAwareModule.counterInstantiations == 0

    expect:

    counter.increment() == 1
    counter.increment() == 2

    ReloadAwareModule.counterInstantiations == 1

    when:

    def classURL = CounterImpl.getResource("CounterImpl.class")
    def classFile = new File(classURL.toURI())

    touch classFile

    update()

    // Check that the internal state has reset

    assert counter.increment() == 1

    then:

    ReloadAwareModule.counterInstantiations == 2
    ReloadAwareModule.counterReloads == 1
  }

  def createInvalidImplementationClass() {
    def internalName = PlasticInternalUtils.toInternalName CLASS

    createClassWriter(internalName, "java/lang/Object", ACC_PUBLIC).with {

      visitMethod(ACC_PROTECTED, "<init>", "()V", null, null).with {
        visitVarInsn ALOAD, 0
        visitMethodInsn INVOKESPECIAL, "java/lang/Object", "<init>", "()V"
        visitInsn RETURN
        visitMaxs 1, 1
        visitEnd()
      }

      visitEnd()

      writeBytecode it, internalName
    }
  }


  def touch(File f) {
    long startModified = f.lastModified();

    int index = 0;

    while (true) {
      f.setLastModified System.currentTimeMillis()

      long newModified = f.lastModified()

      if (newModified != startModified) {
        return;
      }

      // Sleep an ever increasing amount, to ensure that the filesystem
      // catches the change to the file. The Ubuntu CI Server appears
      // to need longer waits.

      Thread.sleep 50 * (2 ^ index++)
    }
  }
}
