# Cron scheduler app for k8s

[![CI](https://github.com/boolivar/k8ron/workflows/CI/badge.svg)](https://github.com/boolivar/k8ron/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/boolivar/k8ron/graph/badge.svg?token=ATJ1977TT8)](https://codecov.io/gh/boolivar/k8ron)
[![release](https://img.shields.io/github/v/release/boolivar/k8ron)](https://github.com/boolivar/k8ron/releases/latest)
[![](https://img.shields.io/docker/v/boolivar/k8ron?label=dockerhub)](https://hub.docker.com/r/boolivar/k8ron)

Schedule example: https://github.com/boolivar/k8ron/blob/master/app/src/test/resources/k8ron-config.yml

## Example manifest to run k8ron app in k8ron namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: k8ron

---

apiVersion: v1
kind: ServiceAccount
metadata:
  namespace: k8ron
  name: k8ron

---

apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: cluster-resource-reader
rules:
- apiGroups: [""]
  resources: ["pods", "services", "endpoints"]
  verbs: ["get", "list", "watch"]

---

apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: k8ron
  name: config-reader
rules:
- apiGroups: [""]
  resources: ["configmaps", "secrets"]
  verbs: ["get", "list", "watch"]

---

apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: read-resources-global
subjects:
- kind: ServiceAccount
  namespace: k8ron
  name: k8ron
roleRef:
  kind: ClusterRole
  name: cluster-resource-reader
  apiGroup: rbac.authorization.k8s.io

---

apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  namespace: k8ron
  name: read-config
subjects:
- kind: ServiceAccount
  namespace: k8ron
  name: k8ron
roleRef:
  kind: Role
  name: config-reader
  apiGroup: rbac.authorization.k8s.io

---

kind: Deployment
apiVersion: apps/v1
metadata:
  namespace: k8ron
  name: k8ron
  labels:
    app.kubernetes.io/name: k8ron
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: k8ron
  template:
    metadata:
      labels:
        app.kubernetes.io/name: k8ron
    spec:
      containers:
        - name: k8ron
          image: boolivar/k8ron:1.0.0
          ports:
            - containerPort: 8080
              protocol: TCP
      serviceAccountName: k8ron

---

apiVersion: v1
kind: Service
metadata:
  namespace: k8ron
  name: k8ron
spec:
  selector:
    app.kubernetes.io/name: k8ron
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
```
