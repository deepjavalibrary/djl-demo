# Deploy Distributed Inference Service with DJL Serving and LWS on GPUs

In this example, we will use LeaderWorkerSet to deploy a distributed inference service with DJL Serving on GPUs.    
[DJL Serving](https://github.com/deepjavalibrary/djl-serving) supports distributed tensor-parallel inference and serving.

## Set up an EKS cluster

### Install eksctl

Follow the [guide](https://docs.aws.amazon.com/emr/latest/EMR-on-EKS-DevelopmentGuide/setting-up-eksctl.html) on how to install eksctl.

```shell
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
eksctl version
```

### Install kubectl

Follow the [guide](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/) on how to install kubectl.

```shell
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client
```

### Create a EKS cluster

```shell
eksctl create cluster -f eks-cluster.yaml
```

## Install LeaderWorkerSet

Follow the [guide]((https://github.com/kubernetes-sigs/lws/blob/main/docs/setup/install.md)) on how to install LWS.

## Deploy LeaderWorkerSet of DJL Serving

We use LeaderWorkerSet to deploy.

```shell
kubectl apply -f lws.yaml
```

Verify the status of the LMI pods

```shell
kubectl get pods
```

Should get an output similar to this

```shell
NAME      READY   STATUS    RESTARTS   AGE
lmi-0     0/1     Running   0          7s
lmi-0-1   1/1     Running   0          7s
```

Check the leader node logs.

```shell
kubectl logs lmi-0
```

Check the worker node logs.

```shell
kubectl logs lmi-0-1
```

## Deploy ClusterIP Service

Apply the `service.yaml` manifest

```shell
kubectl apply -f service.yaml
```

Use `kubectl port-forward` to forward local port 8080 to a pod.

```shell
kubectl port-forward svc/lmi-leader 8080:8080
```

The output should be similar to the following

```shell
Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
```

## Serve the Model

Open another terminal and send a request

```shell
curl -X POST http://127.0.0.1:8080/invocations \
  --connect-timeout 60 \
  -H "Content-type: application/json" \
  -d '{"inputs":"The new movie that got Oscar this year","parameters":{"max_new_tokens":100}}'
```

The output should be similar to the following

```json
{"generated_text": " is called \"The Shape of Water\". It's about a mute woman who falls in love with a mute man. It's a beautiful movie.\nI saw it. It was beautiful."}
```
