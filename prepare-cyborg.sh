#!/bin/bash

if ! ([ -e "build.gradle" ] && [ -e "settings.gradle" ]); then
    _pwd=`pwd`
    echo "Folder '$_pwd' doesn't seem like an Android project..."
    exit 1
fi

modulesToAdd=()
modulesToMigrate=("belog" "cyborg-core" "reflection" "module-manager" "nu-art-core")
for repoName in "${modulesToMigrate[@]}"
do
    echo "--------- ${repoName} -----------"
    if ! grep -q SomeString "$repoName"; then
        modulesToAdd[${#modulesToAdd[*]}]=${repoName}
    fi

    echo "Cloning repo: ${repoName}"
    git clone "git@github.com:nu-art/${repoName}.git"
done

for moduleName in "${modulesToAdd[@]}"
do
    echo "include ':${moduleName}'"
done

