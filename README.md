# ns3asy-bindings [![Build Status](https://travis-ci.org/gscaparrotti/ns3asy-bindings.svg?branch=develop)](https://travis-ci.org/gscaparrotti/ns3asy-bindings)

> Java bindings for ns3 and ns3asy 

---

## Info

[ns3](https://www.nsnam.org/) is a very powerful simulator, but the only way to write simulation for it is to write C++ code. With this
project, it is now possible to have access to some functionalities of ns3 from Java. 

This project is based on [ns3asy](https://github.com/gscaparrotti/ns3asy), which provides access to ns3 using pure C functions. 

Please refer to it for more information. 

To use ns3asy-bindings, you'll have to compile ns3asy and set the `LD_LIBRARY_PATH` environment variable
to the folder which contains ns3asy's and ns3's `*.so` files. 

