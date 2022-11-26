# Alphabet

#### NOTE: Most things have more in depth explantion in their files

<br>

## **Features**
- **Movement** (Auto switches based on the current robot count)
  - **Wave Surfing** for 1v1
  - **Minimum risk** for melee
<br><br>
- **Shooting** (Auto switches with VirtualGun System)
  - **Pattern** Matching (By far the strongest)
  - **Linear** (Takes the current velocity + heading and does math to find the best shot)
  - **Head On** (Self explanatory)
  - **Guess Factor** (Not very good mostly just copied from a tutorial to test, **it's never used**)
<br><br>
- **Data collection**
  - **Virtual guns** (Shoots virtual bullets and stores if they hit or not)
  - **Virtual Leaderboard** (Keeps track of how long bots survive to estimate their leaderboard placement)
  - **Enemy Snapshots** (Every time the radar scans a enemy it's current info is added into a "snapshot" list)
<br><br>

## **Other Notable Features**
  - **ComponentCore**
    <br>The robot is broken up into multiple files this helps TONS with debugging and makes it easier to read.
    <br>To join all the files together I made ComponentCore, at the start I register each component list this.
    ```java
    componentCore.registerComponent(new Radar());
    ```
    Then for every event I only call something like ``eventCore.onScannedRobot()`` and it goes through the components
    and calls them.
    <br>This is great and all but I didn't always want to call all the components. For example I wouldn't want to call the
    <br> ``onScannedRobot`` of all the guns at once. So to solve this I made something called a ``eventConditional`` which is checked before calling the component.
    <br> Here is a example of the ``ON_SCANNED_ROBOT`` eventConditional for ``SurfMovement``.
    ```java
    componentCore.setEventConditional("SurfMovement", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
        return alphabet.movementMode == alphabet.MOVEMENT_SURFING;
    });
    ```
  - **MathUtils** (A collection of *mostly* common math functions)
  - **Statistics** (At the end of battle it will print some helpful stats used for measuring performance)
  - **ShadowGun**
    <br>I really wanted this to work but it doesn't integrate well with the rest of the robot.
    <br>Mainly because the robot wasn't designed to shoot at a selected target instead it
    <br>Just shoots at the last scanned enemy. I might try to fix this in the future.