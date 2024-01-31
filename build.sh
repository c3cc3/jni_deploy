set -x
javac -classpath ${FQ_LIB_HOME}/FileQueueJNI.jar TestDeQXA.java
javac -classpath ${FQ_LIB_HOME}/FileQueueJNI.jar TestEnQ_loop.java
javac -classpath ${FQ_LIB_HOME}/FileQueueJNI.jar:.:json-20210307.jar AgentJson_enQ.java
javac -classpath ${FQ_LIB_HOME}/FileQueueJNI.jar:.:json-20210307.jar AgentJson_deQ.java
#
