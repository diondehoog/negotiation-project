function [yMon, bMon] = monConcBid(y, b, n)
% Given a biddingSpace consisting of a vector with utilities and a matrix
% with for each utily the bid that belongs to it, returns the first n bids
% if monotonic concession is used
    yCop = y;
    bCop = b;
    yMon = zeros(1, n);
    bMon = zeros(size(b, 1), n);
    for i = 1:n
        [m, I] = max(yCop);
        yMon(i) = m;
        bMon(:, i) = bCop(:, I);
        yCop(I) = [];
        bCop(:, I) = [];
    end
end