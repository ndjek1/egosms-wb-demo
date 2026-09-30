var templateStatus = 'ALL';
var templateSearch = '';

function filterTemplates(button, search) {
    if (button) {
        templateStatus = button.getAttribute('data-status');

        var filters = document.querySelectorAll('.wa-filter');
        for (var filterIndex = 0; filterIndex < filters.length; filterIndex++) {
            filters[filterIndex].classList.remove('active');
        }
        button.classList.add('active');
    }

    if (search !== null) {
        templateSearch = search.toLowerCase();
    }

    var cards = document.querySelectorAll('.template-card[data-status]');
    for (var cardIndex = 0; cardIndex < cards.length; cardIndex++) {
        var card = cards[cardIndex];
        var status = card.getAttribute('data-status');
        var searchableText = (card.getAttribute('data-search') || '').toLowerCase();
        var statusMatches = templateStatus === 'ALL' || status === templateStatus;
        var searchMatches = !templateSearch || searchableText.indexOf(templateSearch) >= 0;

        card.style.display = statusMatches && searchMatches ? 'flex' : 'none';
    }
}
