LOCAL_SONAR_PROJECT_KEY='antoniocanaveral_jwt-challenge'
LOCAL_SONAR_ORGANIZATION='antoniocanaveral'
LOCAL_SONAR_LOGIN='85aeaf8d98c60f268986d88d3ad7a3ca7655b5ed'
LOCAL_BRANCH='master'
LOCAL_SFDC_USER='antoniocanaveral@gmail.com'

COVERAGE_REPORT_PATH='target/site/jacoco/jacoco.xml'
SOURCES='.'

echo "   LOCAL_SONAR_PROJECT_KEY: " $LOCAL_SONAR_PROJECT_KEY
echo "   LOCAL_SONAR_ORGANIZATION: " $LOCAL_SONAR_ORGANIZATION
echo "   LOCAL_SONAR_LOGIN: " $LOCAL_SONAR_LOGIN
echo "   LOCAL_BRANCH: " $LOCAL_BRANCH
echo "   LOCAL_SFDC_USER: " $LOCAL_SFDC_USER
echo "   COVERAGE_REPORT_PATH: " $COVERAGE_REPORT_PATH


#CircleCi: Se debe definir el locale ya que no esta definido en la imagen docker.
export LANG="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"


mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Pcoverage \
                              -Dsonar.projectKey="$LOCAL_SONAR_PROJECT_KEY" \
                              -Dsonar.organization="$LOCAL_SONAR_ORGANIZATION" \
                              -Dsonar.branch.name="$LOCAL_BRANCH" \
                              -Dsonar.login="$LOCAL_SONAR_LOGIN" \
                              -Dsonar.verbose=false \

