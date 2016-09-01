aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
        a1 : after call(foo(logical[1, 2, .., 3], *, .., logical[1, .., 2, 3])) : ()

        end

        a2 : after get(*:int[3,3]) : ()

        end

        a3 : after op(-) : ()

        end

        a4 : after op(+) : ()

        end

        a5 : after set(*) : ()

        end
	end
end