**Nothing absolutely nothing**.

I know you must be thinking what a nutcase, but just bear with me for a second.

.

.

*Let’s first get the basics out of the way.*

Main memory is conventionally divided into three blocks,

1. **Code section**- to store code
2. **Stack** - to store variables and function calls in a static way.
3. **Heap** - to store variables dynamically.

.

.

*The key difference between stack and heap:*

Stack only allows for memory chunks to be de-allocated in the exact reverse order as they were allocated in. However no such bound exist for heap. Also by default, a program can only access the stack block.

This works great ….. until ….. we are out of stack memory, and/or we need to break this rule of allocating variables, and de-allocating them. So, What do we do?

*We use Heap memory.*

*Simple, right?*

.

.

.

.

**Problem:** Since we cannot really access it, how do you plan to use it.

**Solution**: Create a variable in stack memory that will store the address of the heap memory block. And, how to operate on the heap memory? Simple, take this address that you have stored, and use it for your operations.

**So these special cases when you want to store address rather than data in a variable, is when you use pointers.**

*So what is the key difference between data variables and pointers?*

**Nothing**, they are both created in stack, both are variables. The only difference being, pointers have a special - hidden, ulterior motive - *some undercover spy*, and variables being the usual, with a definite motive - *some casual man*.





```
A variable is a piece of memory you use to store a value, 8-bit, 16-bit, 32-bit, & 64-bit value. A pointer is a variable that stores (points to) a value that is an address of the piece of memory that stores something.
```

https://www.quora.com/What-is-the-difference-in-a-pointer-and-a-variable