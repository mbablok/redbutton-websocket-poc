# redbutton-websocket-poc

### Infra

1. Init k8s config

```fish
source mb-kubeconfig.fish
```

2. Install cert manager

```
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.19.1/cert-manager.yaml
```

3. Build and deploy service

```
cd redbutton-websocket-poc
./build-and-deploy.sh
```

4. Test

```
curl https://redbutton-websocket.servehttp.com/joke
```
