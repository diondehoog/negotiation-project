function [values, weights] = freqAnalysis(b, n, bc)
% Values:
%   b: should contain a matrix with bids (each row is an issue is column a
%   bid
%   n: the learning rate used for weights
%   bc: number of possible values per issue (e.g. if the issue is laptop
%   brand, then there are 3 possible values (HP, macintosh, Lenovo)
% returns: 
%   The weights for each issue
%   The values for each issue

    % Approximate the weights:
    weights = ones(1, length(bc)) ./ length(bc);
    lastBid = b(:, 1);
    for bid = b(:, 2:size(b, 2))
        for i = 1:length(bid)
            if(lastBid(i) == bid(i))
                weights(i) = weights(i) + n;
            end
        end
        weights = weights ./ sum(weights);
    end
    
    % Approximate the values
    values = cell(1, length(bc));
    for issue = 1:length(bc)
       vis = zeros(1, bc(issue));
       % Find the frequency of each value in the issue
       for i = 1:bc(issue)
           vis(i) = sum(b(issue,:) == i);
       end
       % normalize
       values(issue) = {vis ./ max(vis)};
    end
end