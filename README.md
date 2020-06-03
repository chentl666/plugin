# plugin
占位式插件框架


1.先在项目根目录的 build.gradle 的 repositories 添加:
```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

2.然后在dependencies添加:
```
dependencies {
	...
	implementation 'com.github.chentl666.plugin:1.0.1'
}
```
