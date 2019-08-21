#!/bin/bash

echo "--------------- Prepare for Cyborg ----------------"
echo

gradleSettingsFile="settings.gradle"
gradleBuildFile="push.gradle"

if ! ([ -e ${gradleBuildFile} ] && [ -e ${gradleSettingsFile} ]); then
    _pwd=`pwd`
    echo "--- Folder '$_pwd' doesn't seem like an Android project..."
    exit 1
fi

modulesToAdd=()
modulesToMigrate=("belog" "cyborg-core" "reflection" "module-manager" "nu-art-core")
for repoName in "${modulesToMigrate[@]}"
do
    if ! `grep -q ${repoName} "settings.gradle"`; then
        modulesToAdd[${#modulesToAdd[*]}]=${repoName}
    fi

    if [ -e "${repoName}/.git" ]; then
        echo "--- Repo ${repoName} already exists... pulling..."
        pushd ${repoName} > /dev/null
            git pull
        popd > /dev/null
        echo
        continue
    fi

    git clone "git@github.com:nu-art/${repoName}.git"
    echo
done

echo "--- Adding modules to ${gradleSettingsFile}"
echo

for moduleName in "${modulesToAdd[@]}"
do
    echo "include ':${moduleName}'" >> ${gradleSettingsFile}
done

echo "---------------------- DONE -----------------------"
