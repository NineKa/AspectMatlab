
# AspectMATLAB Pattern Weeding and Analysis
## Classification of Patterns
### Primitive Patterns
Primitive patterns are patterns with actual joint point within MATLAB source code. In AspectMATLAB we have following types of primitive pattern:

 - Get/Set pattern *(match to variable 'reading' and 'writing')*
 - Call/Execution pattern *(match to function/subroutine calling and execution)*
 - Loop/LoopHead/LoopBody pattern *(match to the structure of iterate loop)*
 - MainExection pattern *(match to the entry function/script)*
 - Annotation pattern *(match to comment annotation)*
 - Operator pattern *(match to operators)*
### Modifier Patterns
Unlike primitive patterns, modifier patterns do not provide joint points for action weaving, instead, it pose restriction on the primitive pattern which they bound to. In AspectMATLAB we have the follow types of modifier pattern:

 - Dimension pattern *(restrict the shape of the return variable)*
 - Type pattern *(restrict the type of the return variable)*
 - Scope pattern *(restrict the scope of pattern searching)*
### Compound Patterns
AspectMATLAB compiler further allow us to use *And(&)*, *Or(|)* and *Not(~)* operation to construct much more complex pattern. We define the type of the compound patterns as follows:

| And               | primitive pattern | modifier pattern  |
|-------------------|-------------------|-------------------|
| primitive pattern | primitive pattern | primitive pattern |
| modifier pattern  | primitive pattern |  modifier pattern |

Noticing that a primitive pattern join a modifier pattern using *And* operator will result a primitive pattern. After a modifier pattern join to a primitive pattern, we consider the modifier pattern is 'bounded' to the primitive pattern, posing restrictions to the primitive pattern it binds, and will remove from further analysis (bottom-up). Consider pattern,
```matlab
(get(x) & istype(double)) & call(foo(int[3,..,3]))
```
During the bottom up analysis, the pattern ``` istype(double)``` is bounded to pattern ```get(x)``` and has no influence to the primitive pattern ```call(foo(int[3,..,3]))``` which in its higher level.

| Or                | primitive pattern | modifier pattern |
|-------------------|-------------------|------------------|
| primitive pattern | primitive pattern |      invalid     |
| modifier pattern  |      invalid      | modifier pattern |

Unlike the *And* operation with result in no invalid result, the *Or* analysis will reject attempting to join a primitive pattern and a modifier pattern. Attempting to join these two kinds of pattern will result in a confusing result, i.e. the modifier pattern side will have no actual joint points to weave the code. For example,
```matlab
get(x) | istype(double)
```
such pattern will satisfied if we found something return a double typed variable. However, this does not provide us a actual joint point to weave the code (get? set?). Thus we should reject such pattern compound during analysis.

| Not | primitive pattern | modifier pattern |
|-----|-------------------|------------------|
|     | invalid           | modifier pattern |

Finally, we can construct our compound pattern using *NOT* operator. In AspectMATLAB, we only allow the *NOT* operator applies on modifier pattern. A *NOT* operator applied primitive pattern should be rejected because it provide a ambiguous joint points. For example, ```~get(x)``` will result in a ambiguous joint point (set? call? execution?).

##Cosntruction of patterns
In AspectMATLAB, the pattern first get parsed into AstNodes defined in McAST, then transformed into structure defined in package *abstractPattern* using *AbstractBuilder*, following five basic steps:

 1. expansion of pattern
 2. analysis and weeding on type of pattern
 3. construct a skeleton of patterns (only contains primitive pattern)
 4. attach modifier on primitive patterns
 5. reduce pattern using logical equivalence (makes further analysis and weeding easier)

### Expansion of Pattern
In this step, we focus on substitute identifier which refers to a pattern defined in *Patterns* section into real AstNodes tree. (i.e. remove the ast.Name nodes and attach ast.Expr to its corresponding position, using ```treeCopy()``` method). During this stage, the existence and recursive dependency will be check. If the identifier is undefined, or has a circular dependency, and error will be thrown.
Example on circular dependency check:
``` matlab
aspect_ demo
	patterns
		p : p  %pattern p is depend on itself
	end
	actions
		a : before p : ()
			% codes here
		end
	end
end
```
Compiler result :
```[Error][3 : 7] cannot resolve the dependency of pattern p```

### Analysis and weeding on type of pattern
During this stage, an analysis discussed in the previous section will be performed. For a pattern to be valid to weave, such pattern should be a primitive pattern.

### Construct a skeleton of pattern
The AspectMATLAB compiler will first construct a skeleton of pattern which only contains the primitive patterns and ignore the modifier pattern.

### Attach the modifier pattern
The modifier pattern is attached to the closest primitive node. For example, if an *And* expression has a primitive pattern on one side and a modifier pattern on the other side, the modifier pattern will be constructed and attach to the primitive pattern.

### Logical reduction
In this phase, we move the nodes that attach to the *And* and *Or* node into lower level, using:
```
(A & B) & C == (A & C) & (B & C)
(A | B) & C == (A & C) | (B & C)
```
By using logical reduction, we can move the modifier onto basic primitive patterns, which make the further analysis phase easier (as we can focus on primitive pattern one by one).

Example:
```matlab
(get(x) | set(x)) & within(function : foo)
```
Compiler result:
```
((get(x : *[..]) & within(function : foo)) | (set(x : *[..]) & within(function : foo)))
```

> Written with [StackEdit](https://stackedit.io/).