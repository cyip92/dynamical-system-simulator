# Background

This program was written as a final project for PHYS 314 at the University of San Diego, which is an upper-level undergraduate Physics class focusing on classical mechanics. The program uses the Euler-Lagrange equations to produce the equations of motion for various physical systems. The solutions to the equations of motion were approximated by symmetrically expanding out derivatives to first order and rearranging terms to produce a recurrence relation for every generalized coordinate.

Systems which were analyzed for the project were the double pendulum, a pendulum with a spring rod, a pendulum swinging from a horizontal mass on a spring, and the swinging Atwood machine. Additional systems can be simulated with a bit of physics work and minor programming for rendering, and some tedious labor-saving code which was used has been included in this repo as well.

# Usage Notes

There are a few preset initial conditions which could be seen using the `run()` method with some integer parameters (as can be seen in the switch case). However, the program is largely meant to be run by modifying the values under the comment blocks denoted `Physical parameters` and `Initial conditions`. The program should run largely "out of the box" if only those parameters are changed, although will only be able to run the already-implemented physical systems.

The base program has the functionality to any combination of: animating the simulation (physical state and energy distribution), logging all coordinate data to a file for later analysis, and generating an animated GIF of the simulation (using GIF-writing found online).

# Expanding Functionality

In order to make the program calculate behavior for another system, the following steps must be done:
- The equations of motion must be written out, using Lagrangian mechanics and the Euler-Lagrange equation for each generalized coordinate.
- Equations of motion must then be expanded out into an iterative form using approximations for all their derivatives. Running `main()` in `DETranslator.java` will do this automatically with some prompts to ensure parsing works properly, returning valid Java code for a method for an individual variable. (This is how methods such as `T1NextDP()` and `RNextAtwood()` were generated.)
- A new entry for `paintComponent()` for the new system needs to be written in order to render the system appropriately.
- Add appropriate initial conditions (and possibly additional physical parameters) to the switch cases in `Displayer.java`
