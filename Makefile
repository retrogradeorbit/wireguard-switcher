GRAALVM = ~/graalvm-ce-19.2.0.1

all: build/wireguard-switcher

clean:
	-rm -rf build target

target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar:
	lein clean
	lein uberjar

analyse:
	$(GRAALVM)/bin/java -agentlib:native-image-agent=config-output-dir=config-dir \
		-jar target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar

native-binary: target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar
	-mkdir build
	$(GRAALVM)/bin/native-image \
		-jar target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar \
		-Dsun.java2d.opengl=false \
		-H:Name=build/wireguard-switcher \
		-H:+ReportExceptionStackTraces \
		-J-Dclojure.spec.skip-macros=true \
		-J-Dclojure.compiler.direct-linking=true \
		-H:ConfigurationFileDirectories=config-dir/ \
		--initialize-at-run-time=dorkbox.util.jna.windows.User32,sun.java2d.opengl \
		--initialize-at-build-time \
		-H:Log=registerResource: \
		--verbose \
		--allow-incomplete-classpath \
		--no-fallback \
		--no-server \
		"-J-Xmx3g" \
		-H:+TraceClassInitialization -H:+PrintClassInitialization

binary:
	lein bin
	cp target/default/wireguard-switcher-0.1.0-SNAPSHOT ./wireguard-switcher
