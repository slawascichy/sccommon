# uruchom dla java IBM musi być bez testów ponieważ używamy Hibernate 5, 
# który skompilowany jest w 1.8 
mvnibm clean install -Dmaven.test.skip
# uruchom dla java 1.7 musi być bez testów ponieważ używamy Hibernate 5, 
# który skompilowany jest w 1.8
mvn7 clean install -Dmaven.test.skip
# uruchom dla java 1.8
mvn clean install