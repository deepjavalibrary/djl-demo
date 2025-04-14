FROM ubuntu:24.04

RUN apt-get update || true \
    && apt-get install -y openjdk-17-jdk-headless python3-pip git git-lfs curl locales \
    && sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen \
    && dpkg-reconfigure --frontend=noninteractive locales \
    && update-locale LANG=en_US.UTF-8 \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

RUN rm /usr/lib/python*/EXTERNALLY-MANAGED \
    && pip3 install jupyter \
    && pip3 cache purge

RUN git clone https://github.com/frankfliu/IJava.git \
    && cd IJava/ && ./gradlew installKernel && cd .. && rm -rf IJava/ \
    && rm -rf ~/.gradle

WORKDIR /home/jupyter

ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8

EXPOSE 8888

ENTRYPOINT ["jupyter", "notebook", "--ip=0.0.0.0", "--no-browser", "--allow-root", "--NotebookApp.token=''", "--NotebookApp.password=''"]
