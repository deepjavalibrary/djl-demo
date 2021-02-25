FROM nvidia/cuda:10.1-base-ubuntu18.04

ARG NB_USER="sagemaker-user"
ARG NB_UID="1000"
ARG NB_GID="100"

RUN apt-get update || true && \
    apt-get install -y sudo && \
    useradd -m -s /bin/bash -N -u $NB_UID $NB_USER && \
    chmod g+w /etc/passwd && \
    echo "${NB_USER}    ALL=(ALL)    NOPASSWD:    ALL" >> /etc/sudoers && \
    # Prevent apt-get cache from being persisted to this layer.
    rm -rf /var/lib/apt/lists/*

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y locales && \
    sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8

RUN apt-get install -y \
    openjdk-11-jdk-headless \
    python3-pip git curl unzip

RUN ln -s /usr/bin/python3 /usr/bin/python & \
    ln -s /usr/bin/pip3 /usr/bin/pip

RUN pip install jupyter ipykernel awscli

#### ADDing GPU Stuff, REMOVE unnecessary stuff later
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    LD_LIBRARY_PATH="${LD_LIBRARY_PATH}:/usr/local/lib" \
    PYTHONIOENCODING=UTF-8 \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    SAGEMAKER_TRAINING_MODULE=sagemaker_mxnet_container.training:main \
    DGLBACKEND=mxnet \
    CUDNN_VERSION=7.6.0.64-1+cuda10.1 \
    NCCL_VERSION=2.4.8-1+cuda10.1

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
    software-properties-common \
    build-essential \
    ca-certificates \
    libcudnn7=${CUDNN_VERSION} \
    cuda-command-line-tools-10-1 \
    cuda-cufft-10-1 \
    cuda-curand-10-1 \
    cuda-cusolver-10-1 \
    cuda-cusparse-10-1 \
    curl \
    emacs \
    git \
    libatlas-base-dev \
    libcurl4-openssl-dev \
    libnccl2=${NCCL_VERSION} \
    libgomp1 \
    libnccl-dev=${NCCL_VERSION} \
    libopencv-dev \
    openssh-client \
    openssh-server \
    vim \
    wget \
    zlib1g-dev \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# Install cuda-dev libraries 
ENV CUDA_LIB_URL=https://developer.download.nvidia.com/compute/cuda/repos/ubuntu1804/x86_64
RUN wget ${CUDA_LIB_URL}/libcublas10_10.2.1.243-1_amd64.deb \
    ${CUDA_LIB_URL}/libcublas-dev_10.2.1.243-1_amd64.deb \
    ${CUDA_LIB_URL}/cuda-curand-dev-10-1_10.1.243-1_amd64.deb \
    ${CUDA_LIB_URL}/cuda-cusolver-dev-10-1_10.1.243-1_amd64.deb \
    ${CUDA_LIB_URL}/cuda-nvrtc-10-1_10.1.243-1_amd64.deb \
    ${CUDA_LIB_URL}/cuda-nvrtc-dev-10-1_10.1.243-1_amd64.deb \
 && dpkg -i libcublas10_10.2.1.243-1_amd64.deb \
    libcublas-dev_10.2.1.243-1_amd64.deb \
    cuda-curand-dev-10-1_10.1.243-1_amd64.deb \
    cuda-cusolver-dev-10-1_10.1.243-1_amd64.deb \
    cuda-nvrtc-10-1_10.1.243-1_amd64.deb \
    cuda-nvrtc-dev-10-1_10.1.243-1_amd64.deb \
 && apt-get install -f \
 && rm libcublas10_10.2.1.243-1_amd64.deb \
    libcublas-dev_10.2.1.243-1_amd64.deb \
    cuda-curand-dev-10-1_10.1.243-1_amd64.deb \
    cuda-cusolver-dev-10-1_10.1.243-1_amd64.deb \
    cuda-nvrtc-10-1_10.1.243-1_amd64.deb \
    cuda-nvrtc-dev-10-1_10.1.243-1_amd64.deb


# Install Java kernel
RUN git clone https://github.com/frankfliu/IJava.git
RUN cd IJava/ && ./gradlew zK && \
    unzip -q build/distributions/ijava-1.3.0.zip -d ijava-kernel && \
    cd ijava-kernel && \
    python install.py --sys-prefix && \
    cd ../.. && rm -rf IJava/ && rm -rf ~/.gradle

RUN jupyter kernelspec list

ENV SHELL=/bin/bash
ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8
USER $NB_UID

WORKDIR /home/jupyter
