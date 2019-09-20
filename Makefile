GRAALVM = ~/graalvm-ce-19.2.0.1

build/wireguard-switcher:
	-mkdir build
	lein clean
	lein uberjar
	$(GRAALVM)/bin/native-image \
		-jar target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar \
		-H:Name=build/wireguard-switcher \
		-H:+ReportExceptionStackTraces \
		-J-Dclojure.spec.skip-macros=true \
		-J-Dclojure.compiler.direct-linking=true \
		-H:ReflectionConfigurationFiles=graal-configs/reflection.json \
		--initialize-at-build-time  \
		-H:Log=registerResource: \
		--verbose \
		--no-fallback \
		--no-server \
		"-J-Xmx3g"
