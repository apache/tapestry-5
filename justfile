gradle-options := "--watch-fs"

# Print all recipes
default:
	just --list

# Deploys a snapshot of one subproject to the local Maven repository
local-snapshot artifact:
	./gradlew {{artifact}}:publishToMavenLocal {{gradle-options}} -Dci=true
	
# Deploys a snapshot of every subproject to the local Maven repository
local-snapshot-full:
	./gradlew publishToMavenLocal {{gradle-options}} -Dci=true
	
# Deploys a tapestry-core snapshot to the local Maven repository
tapestry-core-maven-local-snapshot:
	./gradlew tapestry-core:publishToMavenLocal {{gradle-options}} -Dci=true

# Deploys a snapshot to the ASF snapshots repository
snapshot:
	git stash
	./gradlew clean generateRelease {{gradle-options}} -Dci=true
	git stash pop

# Deploys a release to the ASF staging repository
release:
	git stash
	./gradlew clean generateRelease {{gradle-options}}
	git stash pop

# Builds Tapestry without running tests
build:
	./gradlew build -x test

# Cleans all Tapestry artifacts
clean:
	./gradlew clean

# Cleans all Tapestry artifacts and rebuilds them
clean-build:
	./gradlew clean build -x test

# Compiles and generates all JavaScript files from CoffeeScript
generate-javascript:
	./gradlew tapestry-core:compileCoffeeScript tapestry-core:compileProcessedCoffeeScript tapestry-core:compileTestCoffeeScript
