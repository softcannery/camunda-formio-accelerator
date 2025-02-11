#
# Camunda Platform Accelerator for Form.io Community License v1.0
#
# This Camunda Platform Accelerator for Form.io Community License v1.0 (“this Agreement”) sets
# forth the terms and conditions on which Soft Cannery LTD. (“the Licensor”) makes available
# this software (“the Software”). BY INSTALLING, DOWNLOADING, ACCESSING, USING OR DISTRIBUTING
# THE SOFTWARE YOU INDICATE YOUR ACCEPTANCE TO, AND ARE ENTERING INTO A CONTRACT WITH,
# THE LICENSOR ON THE TERMS SET OUT IN THIS AGREEMENT. IF YOU DO NOT AGREE TO THESE TERMS,
# YOU MUST NOT USE THE SOFTWARE. IF YOU ARE RECEIVING THE SOFTWARE ON BEHALF OF A LEGAL ENTITY,
# YOU REPRESENT AND WARRANT THAT YOU HAVE THE ACTUAL AUTHORITY TO AGREE TO THE TERMS AND
# CONDITIONS OF THIS AGREEMENT ON BEHALF OF SUCH ENTITY. “Licensee” means you, an individual,
# or the entity on whose behalf you are receiving the Software.
#
# Permission is hereby granted, free of charge, to the Licensee obtaining a copy of this
# Software and associated documentation files, to deal in the Software without restriction,
# including without limitation the rights to use, copy, modify, merge, publish, distribute,
# sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
# is furnished to do so, subject in each case to the following conditions:
#
# Condition 1: If the Licensee distributes the Software or any derivative works of the Software,
# the Licensee must attach this Agreement.
#
# Condition 2: Without limiting other conditions in this Agreement, the grant of rights under
# this Agreement does not include the right to provide Commercial Product or Service. Written
# permission from the Licensor is required to provide Commercial Product or Service.
#
# A “Commercial Product or Service” is software or service intended for or directed towards
# commercial advantage or monetary compensation for the provider of the product or service
# enabling parties to deploy and/or execute Commercial Product or Service.
#
# If the Licensee is in breach of the Conditions, this Agreement, including the rights granted
# under it, will automatically terminate with immediate effect.
#
# SUBJECT AS SET OUT BELOW, THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
# TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.
#
# NOTHING IN THIS AGREEMENT EXCLUDES OR RESTRICTS A PARTY’S LIABILITY FOR (A) DEATH OR PERSONAL
# INJURY CAUSED BY THAT PARTY’S NEGLIGENCE, (B) FRAUD, OR (C) ANY OTHER LIABILITY TO THE EXTENT
# THAT IT CANNOT BE LAWFULLY EXCLUDED OR RESTRICTED.
#

FROM alpine:3

RUN apk add --no-cache openjdk17-jre-headless bash

ENV PORT=8080
ENV CLASSPATH=/opt/lib
EXPOSE 8080

# copy pom.xml and wildcards to avoid this command failing if there's no target/lib directory
COPY pom.xml target/lib* /opt/lib/

# NOTE we assume there's only 1 jar in the target dir
# but at least this means we don't have to guess the name
# we could do with a better way to know the name - or to always create an app.jar or something
COPY target/*.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-jar", "app.jar"]