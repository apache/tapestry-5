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

_deploy_branch branch extra-options:
	echo "Releasing branch: {{branch}} with Gradle extra options '{{extra-options}}'"
	# Fail if there are untracked files or uncommitted changes
	#git diff --quiet && git diff --cached --quiet || echo "\nThere are untracked files or uncommitted changes!\n" && git status && false
	#git checkout master
	#./gradlew clean generateRelease {{gradle-options}} {{extra-options}}

_deploy_javax extra-options: (_deploy_branch "javax" extra-options)
_deploy_master extra-options: (_deploy_branch "master" extra-options)

# Deploys a snapshot to the ASF snapshots repository
snapshot: (_deploy_branch "master" "-Dci=true") (_deploy_branch "javax" "-Dci=true")

# Deploys a release to the ASF staging repository
release version: (_deploy_branch "master" "") (_deploy_branch "javax" "")
	#git checkout master
	#git tag {{version}}
	#git push --tags
	#git checkout javax
	#git tag {{version}}-javax
	#git push --tags

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
