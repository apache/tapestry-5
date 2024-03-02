#!/bin/bash

# A script to do anything that needs to be done before Gradle runs on Jenkins

NETTY_VERSION=4.1.79.Final

# Ugly workaround to https://github.com/gradle/gradle/issues/18519
# Gradle dependency resolution for capability conflict does not work for 
# transitive Maven dependencies with a classifier
# So we resolve these dependencies ourselves. :D
mkdir -p /home/$USER/.m2/repository/io/netty/netty-transport-native-epoll/$NETTY_VERSION/
mkdir -p /home/$USER/.m2/repository/io/netty/netty-transport-native-kqueue/$NETTY_VERSION/
curl https://repo1.maven.org/maven2/io/netty/netty-transport-native-epoll/$NETTY_VERSION/netty-transport-native-epoll-$NETTY_VERSION.pom -o /home/$USER/.m2/repository/io/netty/netty-transport-native-epoll/$NETTY_VERSION/netty-transport-native-epoll-$NETTY_VERSION-linux-x86_64.pom
curl https://repo1.maven.org/maven2/io/netty/netty-transport-native-epoll/$NETTY_VERSION/netty-transport-native-epoll-$NETTY_VERSION.jar -o /home/$USER/.m2/repository/io/netty/netty-transport-native-epoll/$NETTY_VERSION/netty-transport-native-epoll-$NETTY_VERSION-linux-x86_64.jar
curl https://repo1.maven.org/maven2/io/netty/netty-transport-native-kqueue/$NETTY_VERSION/netty-transport-native-kqueue-$NETTY_VERSION.pom -o /home/$USER/.m2/repository/io/netty/netty-transport-native-kqueue/$NETTY_VERSION/netty-transport-native-kqueue-$NETTY_VERSION-osx-x86_64.pom
curl https://repo1.maven.org/maven2/io/netty/netty-transport-native-kqueue/$NETTY_VERSION/netty-transport-native-kqueue-$NETTY_VERSION.jar -o /home/$USER/.m2/repository/io/netty/netty-transport-native-kqueue/$NETTY_VERSION/netty-transport-native-kqueue-$NETTY_VERSION-osx-x86_64.jar
