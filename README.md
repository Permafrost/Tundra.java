# Tundra.java ❄

This is the pure Java layer that underlies [Tundra], a package of
cool services for [webMethods Integration Server] 7.1 and higher.

Please peruse the [API Documentation] for details on the classes and
methods provided by [Tundra.java].

## Related

See [Tundra] and [TundraTN], packages of cool services for
[webMethods Integration Server] and [webMethods Trading
Networks] 7.1 and higher respectively.

## Dependencies

[Tundra.java] is compiled for Java 1.6, and is dependent on the
following [webMethods Integration Server] [JAR] libraries, which are
required to be included on the project's classpath:

* `./common/lib/wm-isclient.jar`
* `./IntegrationServer/lib/wm-isserver.jar`

It is also dependent on the following open source libraries, which
are required to be included on the project's classpath:

* `commons-collections4-4.1.jar` - https://commons.apache.org/proper/commons-collections/
* `commons-compress-1.12.jar` - https://commons.apache.org/proper/commons-compress/
* `commons-csv-1.4.jar` - https://commons.apache.org/proper/commons-csv/
* `hjson-2.1.1.jar` - https://github.com/Permafrost/hjson-java
* `htmlcompressor-1.5.3.jar` - https://code.google.com/p/htmlcompressor
* `jakarta-oro-2.0.8.jar` - http://archive.apache.org/dist/jakarta/oro/
* `javax.json-1.0.jar` - https://jsonp.java.net
* `jscience-4.3.1.jar` - http://jscience.org
* `log4j-1.2.17.jar` - https://logging.apache.org/log4j/1.2/
* `poi-3.17.jar` - https://poi.apache.org/
* `poi-ooxml-3.17.jar` - https://poi.apache.org/
* `poi-ooxml-schemas-3.17.jar` - https://poi.apache.org/
* `snakeyaml-1.18.jar` - http://www.snakeyaml.org
* `unbescape-1.1.6.RELEASE.jar` - http://www.unbescape.org/
* `xmlsec-1.5.8.jar` - http://santuario.apache.org

[JUnit] 4 is required for the unit tests in the project. The
following libraries are required to be included on the test
classpath:

* `junit.jar`
* `hamcrest-core.jar`

## Contributions

1. Check out the latest master to make sure the feature hasn't been
   implemented or the bug hasn't been fixed yet.
2. Check out the issue tracker to make sure someone already hasn't
   requested it and/or contributed it.
3. Fork the project.
4. Start a feature/bugfix branch.
5. Commit and push until you are happy with your contribution.
6. Make sure to add tests for it. This is important so it won't
   break in a future version unintentionally.

Please try not to mess with the project version, or history. If you
want your own version please isolate it to its own commit, so it can
be cherry-picked around.

## Copyright

Copyright &copy; 2015 Lachlan Dowding. See the [LICENSE] file for
further details.

[API Documentation]: <http://permafrost.github.io/Tundra.java/>
[JAR]: <http://en.wikipedia.org/wiki/JAR_(file_format)>
[JUnit]: <http://junit.org/>
[LICENSE]: <https://github.com/Permafrost/Tundra.java/blob/master/LICENSE>
[Tundra]: <https://github.com/Permafrost/Tundra>
[TundraTN]: <https://github.com/Permafrost/TundraTN>
[Tundra.java]: <https://github.com/Permafrost/Tundra.java>
[webMethods Integration Server]: <http://www.softwareag.com/corporate/products/wm/integration/products/ai/overview/default.asp>
[webMethods Trading Networks]: <http://www.softwareag.com/corporate/products/wm/integration/products/b2b/overview/default.asp>
