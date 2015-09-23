close all, clc, clear all;
source = [1; 4];
destination = [3; 6];
control = [0.5; 6.5];

tRange = 0:0.01:1;
points = zeros(2, length(tRange));
idx = 1;
for t = tRange
    points(:, idx) = quadraticBezier(source, destination, control, t);
    idx = idx + 1;
end
points = points';

hold on
plot([2, 2], [0,5], 'b');
plot([2, 6], [5,5], 'b');
plot([0, 0], [0,7], '--b');
plot([0, 6], [7,7], '--b');
scatter(points(:,1), points(:,2), 50, 'MarkerFaceColor', 'r');
% scatter(points2(:,1), points2(:,2), 50, 'MarkerFaceColor', 'b');
% scatter(points3(:,1), points3(:,2), 50, 'MarkerFaceColor', 'y');
xlim([-1, 7]);
ylim([-1, 7]);
hold off






