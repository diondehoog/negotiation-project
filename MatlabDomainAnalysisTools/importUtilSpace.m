function [v, w, issueNames, itemNames] = importUtilSpace(filename)
% Given a genius-compatible xml file with a utility space, imports this 
% utilities into values v and weights per issue w. 
    res = xmlread(fullfile(pwd, filename));
    issues = res.getElementsByTagName('issue');
    ic = issues.getLength;
    v = cell(1, ic);
    w = zeros(1, ic);
    issueNames = cell(1, ic);
    itemNames = cell(1, ic);
    for k = 0:issues.getLength-1
        issue = issues.item(k);
        idx = str2double(char(issue.getAttribute('index')));
        name = char(issue.getAttribute('name'));
        issueNames{idx} = name;
        items = issue.getElementsByTagName('item');
        curIssueItemNames = cell(1, items.getLength);
        curIssueItemValues = zeros(1, items.getLength);
        for k2 = 0:items.getLength-1
            item = items.item(k2);
            itemIdx = str2double(char(item.getAttribute('index')));
            itemName = char(item.getAttribute('value'));
            itemCost = str2double(char(item.getAttribute('evaluation')));
            curIssueItemNames{itemIdx} = itemName;
            curIssueItemValues(itemIdx) = itemCost;
        end
        itemNames{idx} = curIssueItemNames;
        v{idx} = curIssueItemValues;
    end
    weights = res.getElementsByTagName('weight');
    for k = 0:weights.getLength - 1
        weight = weights.item(k);
        idx = str2double(char(weight.getAttribute('index')));
        value = str2double(char(weight.getAttribute('value')));
        w(idx) = value;
    end
end