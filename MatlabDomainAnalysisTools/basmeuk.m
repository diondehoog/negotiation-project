% Read Party domain xml files:
clear
[v1, w1, issueNames, itemNames] = importUtilSpace('../genius/etc/templates/partydomain/party_utility_kh.xml');
[v2, w2, ~, ~] = importUtilSpace('../genius/etc/templates/partydomain/party_utility_cath.xml');

[v, w] = mergeUtilSpaces(v1, w1, v2, w2);
v = normalizeValues(v);

% Calculate all utilities/bids
[bs, bids] = biddingSpace(v, w);

plot(bs(1,:), bs(2,:), '.'); hold on;

% Calculate and show the nash point
[ns I] = nash(bs);
optItems = bids(:, I);
for k = 1:length(itemNames)
    issue = itemNames{k};
    fprintf('The best option for %s is %s\n', issueNames{k}, issue{optItems(k)});
end
