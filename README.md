# KoloMITM-Android
KoloMITM implementation on Android

### Introduction
KoloMITM-Android is a implementation of KoloMITM, which supports Android platform. You can sign in your Minecraft: Bedrock account and start MITM to modify game packets. It uses KoloMITM, Cloudburst protocol, MinecraftAuth and network libraries etc. These approaches can help you to play game easily.

### Build & Running
To build this project, you need to clone and build KoloMITM initially.

#### 1.Clone KoloMITM

KoloMITM uses Git submodules, to clone:

```shell
# Clone with submodules
git clone https://github.com/LibKolo/KoloMITM.git --recursive
```

#### 2.Build and publish KoloMITM

To Build KoloMITM:

```shell
# Enter the KoloMITM folder
cd KoloMITM

# Build and publish KoloMITM to maven local
./gradlew publishMavenPublicationToMavenLocal
```

#### 3.Clone KoloMITM-Android

```shell
# Clone KoloMITM-Android
git clone https://github.com/LibKolo/KoloMITM-Android.git
```

#### 4.Build KoloMITM-Android

To Build Debug APK:

```shell
# Build debug APK
./gradlew assembleDebug
```

To Build Release APK:

```shell
# Build release APK
./gradlew assembleRelease
```

Now, the APK will be generated in `app/build/outputs/apk/debug` or `app/build/outputs/apk/release`. Just install them on your smartphone.

### Acknowledgements
[Protocol](https://github.com/CloudburstMC/Protocol.git)
| [Network](https://github.com/CloudburstMC/Network.git)
| [ProxyPass](https://github.com/CloudburstMC/ProxyPass.git)