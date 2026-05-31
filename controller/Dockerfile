FROM jenkins/jenkins:lts-jdk21

# Disable the Setup Wizard (since we configure everything programmatically via JCasC)
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Copy the plugins file and install them
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugins -f /usr/share/jenkins/ref/plugins.txt
