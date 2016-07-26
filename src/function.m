function M02()
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  Function Definition Line: (repeat line 1 as a comment here)
%
%
%  Inputs: list each input argument variable name and
%          comment with units (as appropriate):
%  1.
%  2.
%  ...
%
%  Outputs: list each output argument variable name and
%           comment with units (as appropriate):
%  1.
%  2.
%  ...
%
%  Function Description: (write a short description of what this function
%  does)
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  ENGR 13200 Fall 2015
%  Programmer(s) and Purdue Email Address(es):
%  1. Name, login@purdue.edu
%
%  Other Contributor(s) and Purdue Email Address(es):
%  1. Name login@purdue.edu
%
%  Section #:      Team #:
%
%  Assignment #:
%
%  Academic Integrity Statement:
%
%       I/We have not used source code obtained from
%       any other unauthorized source, either modified
%       or unmodified.  Neither have I/we provided access
%       to my/our code to another. The project I/we am/are
%       submitting is my/our own original work.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% --- INPUTS ---
data = load('noisy.txt');

% --- CALCULATIONS ---
time = data(:,1);
t = data(:,2);
avr_y1 = 0;
for index = 1:20
    avr_y1 = avr_y1 + t(index);
end
avr_y1 = avr_y1/20;
dif = 0;
for index = 1:5
    dif = dif + abs(avr_y1-t(index));
end
dif = dif / 5;
finish = 1;
index = 1;

while finish
    if (abs(t(index) - avr_y1) > (5* dif)) || index > length(time)-1
        finish = 0;
    end
    index = index + 1;
end
Time_I = index - 3;
Ts = time(Time_I);
Ys = 0;
min = t(1);
max = t(1);
for index = 1 :Time_I
    Ys = Ys + t(index);
    if max < t(index);
        max = t(index);
    end
    if min > t(index)
        min = t(index);
    end
end
Ys = (Ys - min - max) / (Time_I - 2);
num = length(t);
index = Time_I;
a1 = [t(index+1) t(index+2) t(index+3) t(index+4) t(index+5) t(index+6) t(index+7) t(index+8) t(index+9) t(index+10) ];
var_i = std(a1);
finish = 1;
while finish

    a = [t(index+1) t(index+2) t(index+3) t(index+4) t(index+5) t(index+6) t(index+7) t(index+8) t(index+9) t(index+10) ];
    var_f = std(a);
    if (var_f < var_i / 10) || (index > 189)
        finish = 0;
    end
    index = index + 1;

end
Yss = mean(a);
index = Time_I;
Yt = Ys + (Yss-Ys) * 0.4
finish = 1;
while finish
    if abs(t(index)-Yt)< 2
        Te = time(index);
        finish = 0;
    end
    index = index + 1;
end
% --- OUTPUTS ----
fprintf('Ts = %.2f\nYs = %.2f\n',Ts,Ys);
fprintf('Yss = %.2f\n',Yss);
fprintf('Te = %.2f\n',Te);
% --- RESULTS ----

