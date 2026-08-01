FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

ENV DEBIAN_FRONTEND=noninteractive

# 1. Install Java 17 and base Linux build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless \
    wget \
    unzip \
    curl \
    git \
    build-essential \
    file \
    && rm -rf /var/lib/apt/lists/*

# 2. Configure Environment Paths
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_HOME=/home/vscode/android-sdk
ENV ANDROID_SDK_ROOT=/home/vscode/android-sdk
ENV PATH="${PATH}:${JAVA_HOME}/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

# 3. Setup SDK Directory with full permissions for the vscode user
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    chown -R vscode:vscode /home/vscode/android-sdk

# Switch to non-root vscode user to fetch Android SDK components safely
USER vscode

# 4. Install Android Command Line Tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip && \
    mkdir -p /tmp/cmdline-tools-extracted && \
    unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-extracted && \
    mkdir -p ${ANDROID_HOME}/cmdline-tools/latest && \
    mv /tmp/cmdline-tools-extracted/cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest/ && \
    rm -rf /tmp/cmdline-tools*

# 5. Accept licenses & pre-install Wear OS 5+ SDK platforms (API 34 & 35)
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1 || true && \
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
        "platform-tools" \
        "platforms;android-34" \
        "platforms;android-35" \
        "build-tools;34.0.0" \
        "build-tools;35.0.0"

USER root
