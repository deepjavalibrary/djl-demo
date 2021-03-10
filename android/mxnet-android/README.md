# DJL MXNet Android Template

### Introduction
This template includes 1. how you build the mxnet native library(*libmxnet.so*) for android
2. how you use MXNet native library(*libmxnet.so*) in Android Studio with DJL to run a simple image classification.
DJL team also prepare an unofficial prebuilt `libmxnet.so` on 4 common Android ABI.
You can take advantage of those and skip the step 1.

| Android ABI |
| ----------- |
| [armeabi-v7a](https://djl-ai.s3.amazonaws.com/publish/mxnet-1.8.0/android_native/armeabi-v7a_native.zip) |
| [arm64-v8a](https://djl-ai.s3.amazonaws.com/publish/mxnet-1.8.0/android_native/arm64-v8a_native.zip) |
| [x86](https://djl-ai.s3.amazonaws.com/publish/mxnet-1.8.0/android_native/x86_native.zip) |
| [x86_64](https://djl-ai.s3.amazonaws.com/publish/mxnet-1.8.0/android_native/x86_64_native.zip) |

### 1. Build MXNet from source on Android platform
MXNet Android uses OpenBLAS to do matrix computation. Before we build MXNet from source,
we need to get *libopenblas.so* ready on common Android ABI which are `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64` respectively.

#### Build OpenBlas from source
Here is the minimal script to build openblas assuming you have a base `ubuntu 20.02` image.

##### Prerequisite

```
apt update && apt install -y \
    build-essential \
    ninja-build \
    cmake \
    ccache \
    git \
    curl \
    unzip

# We use NDK r19 version here and use /usr/local as home dir
# You can switch to newer version and other dirs
cd /usr/local
curl -o android-ndk-r19-linux-x86_64.zip -L https://dl.google.com/android/repository/android-ndk-r19-linux-x86_64.zip
unzip android-ndk-r19-linux-x86_64.zip
rm android-ndk-r19-linux-x86_64.zip
export CMAKE_TOOLCHAIN_FILE=/usr/local/android-ndk-r19/build/cmake/android.toolchain.cmake
export TOOLCHAIN=/usr/local/android-ndk-r19/toolchains/llvm/prebuilt/linux-x86_64
```

##### Build OpenBLAS on different arch

```
# latest version
git clone --recursive -b v0.3.12 https://github.com/xianyi/OpenBLAS.git
mkdir /usr/local/openblas-android
cd /usr/local/OpenBLAS

# Build armv7
make \
    TARGET=ARMV7 \
    ONLY_CBLAS=1 \
    CC="$TOOLCHAIN"/bin/armv7a-linux-androideabi21-clang \
    AR="$TOOLCHAIN"/bin/arm-linux-androideabi-ar \
    HOSTCC=gcc \
    ARM_SOFTFP_ABI=1 \
    -j4
make PREFIX=/usr/local/openblas-android NO_SHARED=1 install

# Build armv8
make \
    TARGET=CORTEXA57 \
    ONLY_CBLAS=1 \
    CC=$TOOLCHAIN/bin/aarch64-linux-android21-clang \
    AR=$TOOLCHAIN/bin/aarch64-linux-android-ar \
    HOSTCC=gcc \
    -j4
make PREFIX=/usr/local/openblas-android NO_SHARED=1 install

# Build x86
make \
    TARGET=ATOM \
    ONLY_CBLAS=1 \
    CC="$TOOLCHAIN"/bin/i686-linux-android21-clang \
    AR="$TOOLCHAIN"/bin/i686-linux-android-ar \
    HOSTCC=gcc \
    ARM_SOFTFP_ABI=1 \
    -j4
make PREFIX=/usr/local/openblas-android NO_SHARED=1 install

# Build x86_64
make \
    TARGET=ATOM BINARY=64\
    ONLY_CBLAS=1 \
    CC="$TOOLCHAIN"/bin/x86_64-linux-android21-clang \
    AR="$TOOLCHAIN"/bin/x86_64-linux-android-ar \
    HOSTCC=gcc \
    ARM_SOFTFP_ABI=1 \
    -j4
make PREFIX=/usr/local/openblas-android NO_SHARED=1 install
```

```
# This will tell mxnet where to find the openblas
export OpenBLAS_HOME=/usr/local/openblas-android
```

#### Build MXNet from source
```
git clone --recursive -b 1.8.0 https://github.com/apache/incubator-mxnet.git
mkdir build
cd build

# Build armv7
cmake \
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} \
        -DANDROID_ABI="armeabi-v7a" \
        -DANDROID_STL="c++_static" \
        -DANDROID=ON \
        -DUSE_CUDA=OFF \
        -DUSE_SSE=OFF \
        -DUSE_LAPACK=OFF \
        -DUSE_OPENCV=OFF \
        -DUSE_OPENMP=OFF \
        -DUSE_SIGNAL_HANDLER=ON \
        -DUSE_MKL_IF_AVAILABLE=OFF \
        -G Ninja ..
ninja

# Build armv8
cmake \
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} \
        -DANDROID_ABI="arm64-v8a" \
        -DANDROID_STL="c++_static" \
        -DANDROID=ON \
        -DUSE_CUDA=OFF \
        -DUSE_SSE=OFF \
        -DUSE_LAPACK=OFF \
        -DUSE_OPENCV=OFF \
        -DUSE_OPENMP=OFF \
        -DUSE_SIGNAL_HANDLER=ON \
        -DUSE_MKL_IF_AVAILABLE=OFF \
        -G Ninja ..
ninja

# Build x86
cmake \
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} \
        -DANDROID_ABI="x86" \
        -DANDROID_STL="c++_static" \
        -DANDROID=ON \
        -DUSE_CUDA=OFF \
        -DUSE_SSE=OFF \
        -DUSE_LAPACK=OFF \
        -DUSE_OPENCV=OFF \
        -DUSE_OPENMP=OFF \
        -DUSE_SIGNAL_HANDLER=ON \
        -DUSE_MKL_IF_AVAILABLE=OFF \
        -G Ninja ..
ninja

# Build x86-64
cmake \
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} \
        -DANDROID_ABI="x86_64" \
        -DANDROID_STL="c++_static" \
        -DANDROID=ON \
        -DUSE_CUDA=OFF \
        -DUSE_SSE=OFF \
        -DUSE_LAPACK=OFF \
        -DUSE_OPENCV=OFF \
        -DUSE_OPENMP=OFF \
        -DUSE_SIGNAL_HANDLER=ON \
        -DUSE_MKL_IF_AVAILABLE=OFF \
        -G Ninja ..
ninja
```

If you don't run into any error, you can see `libmxnet.so` in the build folder

### 2. Android Studio Setup
Depending on Android you would like to test, you just need to put the corresponding `libmxnet.so` under the *src/main/jniLibs/{ABI}*.
For example, if you want to run on `armeabi-v7a`, put the right `libmxnet.so` under *src/main/jniLibs/armeabi-v7a*.

Then you're all set. You can run this project and should be able to see the classification result right in the middle.

Here are extra setting I did when I created the project.

1. In `build.gradle`, I included 4 packages `ai.djl:api`, `ai.djl.android:core`, `ai.djl.mxnet:mxnet-engine` and `ai.djl.mxnet:mxnet-model-zoo`
to use DJL API. Because DJL MXNet interact with MXNet engine via JNA, one more dependency called `net.java.dev.jna:jna` is required as well.  
DJL API depends on JNA by default. For Android project, we should use `net.java.dev.jna:jna:5.3.0@aar` instead. We need to exclude duplicate JNA dependencies by using `exclude group: "net.java.dev.jna", module: "jna"`

2. Android system only allows certain location where you can download files from internet.
DJL by default will choose `~/.djl.ai` as directory for keeping models. To make sure we download the model into the right place, we expose a System Property `DJL_CACHE_DIR`.

3. The project involves downloading the model from internet, so I add `<uses-permission android:name="android.permission.INTERNET" />`
and move model loading in AsyncTask.

In terms of DJL API usage, you can find more details on [Official Doc](https://docs.djl.ai/).




