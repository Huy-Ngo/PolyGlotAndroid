# Contribution guideline (in construction)

This is a guide to collaborator(s), future contributors, and notes to myself.

This includes developing process, naming convention, class managements, etc.

## Converting from Java to Kotlin

This project uses Kotlin, a new language for Android development, whereas the project this is based
on ([PolyGlot]) is written in Java. To contribute to the core component
of this project (during the incubation period, i.e. the time I'm writing this), please head to
[PolyGlot] and convert classes into Kotlin.

Please also refer to [Kotlin's official guidelines]

## Word decay

Due to its ambiguity, word decay (which is used in original project) is not encouraged.
Since PolyGlot desktop uses it, here is some reference for you to check when you encounter one:

- Mgr: Manager
- val: value
- ety/etym: etymology.
- def: definition
- proc: pronunciation
- decl: declension
- `comp`: compared (object). use `other` instead
(currently there are some functions I use comp, refractor it later)
-

### Allowed exceptions

- `DictCore`
- doc, id: Common abbreviations
- `it` for iterator: This is recommended in Kotlin guideline.
- `ret`: return value

## Word cases

- For normal variable name and object names, use `camelCase`.
- For constants, use `CAPITAL_CASE_WITH_UNDERSCORE`
- For class name, use `PascalCase`

## Getters and setters

If the property is private and its getter/setter are like this:

```java
class Foo {
    private int bar;
    int getBar() {return this.bar;}
    void setBar(int _bar) {bar = _bar}
}
```

Please consider write it like this

```kotlin
class Foo {
    var bar: Int
}
```

... instead of

```kotlin
class Foo {
    private var bar: Int
    get() = field
    set(value) {
        field = value
    }
}
```

[PolyGlot]: github.com/DraqueT/PolyGlot
[Kotlin's official guidelines]: https://kotlinlang.org/docs/reference/coding-conventions.html