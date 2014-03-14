function vNorm = normalizeValues(v)
    vNorm = cell(1, length(v));
    for i = 1:length(v)
        vCur = v{i};
        mx = 1 ./ max(vCur, [], 2); % Get the scaling values per row.
        vNorm{i} = vCur .* repmat(mx, 1, size(vCur, 2));    
    end
end