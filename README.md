# jmonkeytetris

A tetris game demo, illustrating usage of jmonkeyengine.

Features a use of the following:

* Use of custom application states
* Game loop
* Use of sound effects
* Sample save/load game functionality
* Primitive UI

Game UI:

![logo](https://user-images.githubusercontent.com/822988/182048752-e949e098-68c3-44ec-8034-b4632bd8a12b.png)

![game](https://user-images.githubusercontent.com/822988/182048754-66ee70a6-2493-4dc5-affa-c37cfb58fd6b.png)

## First Start

Install maven 3 then do `mvn clean install` in the application's directory.

Once application compiled, you can start it as follows:

```bash
mvn exec:java
```

## Gameplay Keys

* Left, Right - move falling tetrade left and right
* Up - rotate tetrade
* Down - speed up tetrade to make it fall faster
* Space - pause game
* Q - quick save (default path is `~/.jmonkeytetris/saves/quick`)
* L - quick load
* Esc - escape to title screen or (if already on title screen) exit game

