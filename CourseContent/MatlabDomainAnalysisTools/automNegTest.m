%% 1. 
% Assuming agent A use monotonic concession, what would be it's first 5
% bids?
v = { [0.5 0.3 0.2], [0.2 0.8], [0.1 0.3 0.6] };
w = [0.6 0.3 0.1];
0
[y, b] = biddingSpace(v, w);

[mon, bids] = monConcBid(y, b, 5)

%% 2.
% Agent B would like to model Agent A's preferences by employing 
% 'frequency analysis'
bc = [3 2 3];
n = 0.1;
[v2, w2] = freqAnalysis(bids, n, bc);
w2
celldisp(v2);

% So the estimated weights are: 
%   location: 0.2911
%   duration: 0.4578
%   hotel-quality: 0.2511
% And the estimated values are (in the same order as the paper):
%   location:       1.0000    0.6667         0
%   duration:       0         1
%   hotel-quality:  0.5000    1.0000    1.0000


%% 3. 
% Agent B would like employ a trade-off strategy. Suppose that it wants
% to send out an offer of 0.6. The iso-curve of all bids with utility 6
% is...

isoBids = [2 1 3 1 3;
    2 1 1 1 2;
    1 2 3 1 1];
secBid = bids(:, 2);

d = [];
for bid = isoBids
    d = [d hammingDistance(bid, secBid)];
end
d
% We note that the second bit has the smallest distance, so that means that
% the best bid is: 1 1 2: Antalya, 1 week, 3 star hotel.
