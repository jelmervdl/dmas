function [ point ] = quadraticBezier( source, destination, control, t )
       point = (1 - t) * linearInterpolation(source, control, t) + ...
           t * linearInterpolation(control, destination, t);
end

function [point] = linearInterpolation(source, destination, t)
   point = source + t * (destination - source);
end

