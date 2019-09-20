GRAALVM = ~/graalvm-ce-19.2.0.1

all: build/wireguard-switcher

clean:
	-rm -rf build target

target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar:
	lein clean
	lein uberjar

build/wireguard-switcher: target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar
	-mkdir build
	$(GRAALVM)/bin/native-image \
		-jar target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar \
		-H:Name=build/wireguard-switcher \
		-H:+ReportExceptionStackTraces \
		-J-Dclojure.spec.skip-macros=true \
		-J-Dclojure.compiler.direct-linking=true \
		-H:ReflectionConfigurationFiles=graal-configs/reflection.json \
		--initialize-at-build-time \
		--initialize-at-run-time=dorkbox.util.jna,sun.java2d.opengl,sun.awt.X11.XToolkit,java.awt.Toolkit \
		-H:Log=registerResource: \
		--verbose \
		--allow-incomplete-classpath \
		--no-fallback \
		--no-server \
		"-J-Xmx3g" \
		-H:+TraceClassInitialization -H:+PrintClassInitialization --debug-attach

analyse:
	$(GRAALVM)/bin/java -agentlib:native-image-agent=config-output-dir=config-dir \
		-jar target/uberjar/wireguard-switcher-0.1.0-SNAPSHOT-standalone.jar
