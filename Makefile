compile-clox:
	gcc -std=c11 -Wall -Wextra \
			Crafting\ Interpreters/clox/memory.c \
			Crafting\ Interpreters/clox/chunk.c \
			Crafting\ Interpreters/clox/value.c \
			Crafting\ Interpreters/clox/debug.c \
			Crafting\ Interpreters/clox/scanner.c \
			Crafting\ Interpreters/clox/compiler.c \
			Crafting\ Interpreters/clox/vm.c \
    	    Crafting\ Interpreters/clox/main.c \
    	    -o clox

run-clox:
	./clox
