FROM ubuntu:18.04

ARG NB_USER="sagemaker-user"
ARG NB_UID="1000"
ARG NB_GID="100"

# Setup the "sagemaker-user" user with root privileges.
RUN \
    apt-get update && \
    apt-get install -y sudo && \
    useradd -m -s /bin/bash -N -u $NB_UID $NB_USER && \
    chmod g+w /etc/passwd && \
    echo "${NB_USER}    ALL=(ALL)    NOPASSWD:    ALL" >> /etc/sudoers && \
    # Prevent apt-get cache from being persisted to this layer.
    rm -rf /var/lib/apt/lists/*


ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
    openjdk-11-jdk-headless \
    python3-pip git curl unzip

RUN ln -s /usr/bin/python3 /usr/bin/python & \
    ln -s /usr/bin/pip3 /usr/bin/pip

RUN pip install ipykernel awscli

# Install Java kernel
RUN curl -L https://github.com/SpencerPark/IJava/releases/download/v1.3.0/ijava-1.3.0.zip -o ijava-kernel.zip && \
    unzip -q ijava-kernel.zip -d ijava-kernel && cd ijava-kernel && \
    python install.py --sys-prefix && \
    cd .. && rm -rf ijava-kernel* && rm -rf ~/.gradle

RUN jupyter kernelspec list

ENV SHELL=/bin/bash
USER $NB_UID
