%% 1. compute the nash point
v = { [0.4 0.4 0.2; 0.3 0.2 0.5], [0.3 0.7; 0.4 0.6], [0.5 0.2 0.3; 0.3 0.3 0.4] };
w = [0.5 0.2 0.3; 0.5 0.4 0.1];

[y, b] = biddingSpace(v, w);

[n i] = nash(y)
b(:, i)

plot(y(1,:), y(2,:), '.'); hold on;
plot(y(1,i), y(2,i), '+r'); hold off;