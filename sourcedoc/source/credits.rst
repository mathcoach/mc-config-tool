*******
Credits
*******


Most code in packages ``*.text`` is copied from Apache's StringUtils and Commons Text.

In early versions of this libary, it used these both libraries. But other libraries in my projects
also use these libraries in other version than this library uses. This caused many hard to solve version
conflict problem.

I decided to remove the dependencies and just copy the needed code into the package ``*.text``. Nice
sideeffect: it makes this library smaller.
