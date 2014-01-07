function [v, w] = mergeUtilSpaces(v1, w1, v2, w2)
% Given the utility spaces of two agent, merges them into a combnined
% utility space. Useful for plotting the bidding space later.
    w = [w1; w2];
    v = cell(1, length(v1));
    for i = 1:length(v1)
        v{i} = [v1{i}; v2{i}];
    end
end