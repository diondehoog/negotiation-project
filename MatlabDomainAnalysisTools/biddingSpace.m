function [y bids] = biddingSpace(values, weights)
% Calculates the utilities for all possible bids and returns a matrix with
% each column representing a bid and each row representing an agent. Also
% returns an array containing a list of all bids in the same order as y
% (useful to trace back which bid it was)
% 
% Example:
% Say we have 3 issues we need to solve, and we know the value for each
% value of the issues, we can construct the values cell array. This cell
% array contains for each issue a matrix. Within this matrix each row
% represents and each and each column a value within the issue for that
% agent.
% The weight contain a row for each agent and a column for each 
%   v = { [0.4 0.4 0.2; 0.3 0.2 0.5], [0.3 0.7; 0.4 0.6], [0.5 0.2 0.3; 0.3 0.3 0.4] };
%   w = [0.5 0.2 0.3; 0.5 0.4 0.1];
%   y = biddingSpace(v, w);
    curValues = cell2mat(values(1));
    
    y = [];
    bids = [];
    for i = 1:size(curValues, 2)
        curY = curValues(:, i) .* weights(:, 1);
        curBids = i;
        if (size(values, 2) > 1)
            [rest, restBids] = biddingSpace(values(2:size(values, 2)), weights(:, 2:length(weights)));
            repY = repmat(curY, 1, size(rest, 2));
            repBid = repmat(curBids, 1, size(restBids, 2));
            curY = repY + rest;
            curBids = [repBid; restBids];
        end
        y = [y curY];
        bids = [bids curBids];
    end
end