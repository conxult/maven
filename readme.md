

==generate-binks

  <build>
    <plugins>
      ...
      <plugin>
        <groupId>de.conxult</groupId>
        <artifactId>cx-maven-plugin</artifactId>
        <version>1.0.0</version>
        <executions>
          <execution>
            <goals>
              <goal>generate-binks</goal>
            </goals>
            <configuration>
              <binksMainClass>some.Main</binksMainClass>
              <binksJar>${project.build.directory}/SomeJar.jar</binksJar>
            </configuration>
          </execution>
        </executions>
      </plugin>
      ...
    </plugins>
  </build>

