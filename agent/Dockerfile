FROM jenkins/ssh-agent:latest

USER root

# Install the Docker CLI binary only (not the full daemon).
# The agent talks to a remote DinD sidecar via DOCKER_HOST, so only the client is needed.
ARG DOCKER_VERSION=27.5.1
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates && \
    rm -rf /var/lib/apt/lists/* && \
    ARCH="$(dpkg --print-architecture)" && \
    case "${ARCH}" in \
      amd64) DOCKER_ARCH=x86_64 ;; \
      arm64) DOCKER_ARCH=aarch64 ;; \
      *)     echo "Unsupported arch: ${ARCH}" && exit 1 ;; \
    esac && \
    curl -fsSL "https://download.docker.com/linux/static/stable/${DOCKER_ARCH}/docker-${DOCKER_VERSION}.tgz" \
      | tar xz --strip-components=1 -C /usr/local/bin docker/docker && \
    chmod +x /usr/local/bin/docker

# Fix permissions for the setup-sshd entrypoint which runs as jenkins:
#  - /etc/environment : entrypoint appends env vars for SSH sessions (line 54)
#  - /etc/ssh/        : entrypoint runs ssh-keygen -A to generate host keys (line 72)
RUN touch /etc/environment && chmod 666 /etc/environment && \
    mkdir -p /etc/ssh && chown -R jenkins:jenkins /etc/ssh

USER jenkins
ENV PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
