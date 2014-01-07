function [N, I] = nash(bs)

    prod = ones(size(bs, 2), 1);
    % First calculate the products:
    for i = 1:size(bs, 1)
       prod = prod .* bs(i, :)';
    end
    [N, I] = max(prod);
end