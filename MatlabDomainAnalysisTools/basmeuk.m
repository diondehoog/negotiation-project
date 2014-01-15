% Read Party domain xml files:
clear
[v1, w1, issueNames, itemNames] = importUtilSpace('../genius/etc/templates/partydomain/AI2009/party_user10_utility.xml');
[v2, w2, ~, ~] = importUtilSpace('../genius/etc/templates/partydomain/AI2009/party_user15_utility.xml');

[v, w] = mergeUtilSpaces(v1, w1, v2, w2);
v = normalizeValues(v);

% Calculate all utilities/bids
[bs, bids] = biddingSpace(v, w);
%%
firstpar = 1;
paretoB = [];
for i = 1:size(bs,2)
    curbid = bs(:,i);
    pareto = 1;
    %fprintf('Current bid: ');
    %curbid
    for j = 1:size(bs,2)
        anotherbid = bs(:,j);
        %anotherbid
        if sum(anotherbid > curbid)==2
            %fprintf('This is not pareto optimal\n')
            pareto = 0;
        end
    end
    if pareto
        if firstpar
            paretoB = curbid;
            firstpar = 0;
        else
            paretoB = [paretoB curbid];
        end
    end
end
paretoB = sortrows(paretoB',1)
paretoB = paretoB'
%%
figure
plot(bs(1,:), bs(2,:), '.'); hold on;
plot(paretoB(1,:), paretoB(2,:), '.-r')
title('Pareto frontier')
xlabel('Party Utility 10')
ylabel('Party Utility 15')
print(gcf,'-dpng','-r300','pareto.png');
%%
% Calculate and show the nash point
[ns I] = nash(bs);
optItems = bids(:, I);
for k = 1:length(itemNames)
    issue = itemNames{k};
    fprintf('The best option for %s is %s\n', issueNames{k}, issue{optItems(k)});
end

%%
other = dlmread('~/workspace/Log/20140115124745_OtherHistory.txt', ';');
own = dlmread('~/workspace/Log/20140115124749_OwnHistory.txt', ';');

plot(other);
otherAvg = conv(other, ones(20, 1) ./ 20, 'valid');
plot(otherAvg);
sum(conv(otherAvg, [1 -1], 'valid'))
