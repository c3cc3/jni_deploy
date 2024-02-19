set -x
export JAVA_HOME=/opt/jdk-17
export FQ_WORK_HOME=/home/ums/work/jni_deploy
export FQ_DATA_HOME=/home/ums/work/jni_deploy/enmq
export FQ_LIB_HOME=/home/ums/work/jni_deploy/lib
export PATH=$PATH:/home/ums/work/jni_deploy/bin:$JAVA_HOME
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/ums/work/jni_deploy/lib:$JAVA_HOME/lib:
