mvn install:install-file \
    -DgroupId=com.android \
    -DartifactId=changelibs \
    -Dversion=1.0.0 \
    -DgeneratePom=true \
    -Dpackaging=aar \
    -Dfile=libs/changelibs.aar \
    -DlocalRepositoryPath=/android/packages/apps/UpdateCenter/libs
