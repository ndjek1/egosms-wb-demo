/**
 * PrimeFaces California Layout
 */
PrimeFaces.widget.California = PrimeFaces.widget.BaseWidget.extend({

    init: function(cfg) {
        this._super(cfg);
        this.wrapper = $(document.body).children('.layout-wrapper');
        this.topbar = this.wrapper.children('.layout-topbar');
        this.sidebar = this.wrapper.children('.layout-sidebar');
        this.menuContainer = this.wrapper.children('.sidebar-scroll-content');
        this.layoutMegaMenu = $('#layout-megamenu');
        this.menu = this.jq;
        this.menulinks = this.menu.find('a');
        this.expandedMenuitems = this.expandedMenuitems||[];
        this.menuButton = $('#menu-button');
        this.megaMenuButton = $('#layout-megamenu-button');
        this.topbarMenuButton = $('#topbar-menu-button');
        this.sidebarProfileButton = $('#sidebar-profile-button');

        this.sidebarUserContainer = $('.user-profile');
        this.sidebarUserMenu = $('#sidebar-usermenu');
        this.topbarUserMenu = $('#topbar-usermenu');

        var isSlimMenu = this.wrapper.hasClass('layout-wrapper-slim-sidebar');
        var isHorizontalMenu = this.wrapper.hasClass('layout-wrapper-horizontal-sidebar');

        if(this.sidebarUserMenu.length)
            this.usermenuLinks = this.sidebarUserMenu.find('a');
        if (this.topbarUserMenu.length)
            this.usermenuLinks = this.topbarUserMenu.find('a');

        this.topbarMenuClick = false;
        this.megaMenuClick = false;
        this.rightSidebarClick = false;
        this.sidebarMenuClick = false;
        this.sidebarUserMenuClick = false;

        this.configButton = $('#layout-config-button');
        this.configurator = this.wrapper.children('.layout-config');
        this.configClicked = false;

        this._bindEvents();

        if(!(isSlimMenu && this.isDesktop()) && !(isHorizontalMenu && this.isDesktop())) {
            this.restoreMenuState();
        }
    },

    _bindEvents: function() {
        var $this = this;

        $this.sidebar.off('click.sidebar').on('click.sidebar', function () {
            $this.sidebarMenuClick = true;
        });

        $this.menuButton.off('click.menu').on('click.menu', function (e) {
            $this.sidebarMenuClick = true;

            if ($this.isDesktop()) {
                if ($this.isOverlay())
                    $this.wrapper.toggleClass('layout-wrapper-overlay-sidebar-active');
                else
                    $this.wrapper.toggleClass('layout-wrapper-sidebar-inactive');
            }
            else {
                $this.wrapper.toggleClass('layout-wrapper-sidebar-mobile-active');
            }

            e.preventDefault();
        });

        $this.megaMenuButton.off('click.megamenu').on('click.megamenu', function (e) {
            $this.megaMenuClick = true;

            if ($this.layoutMegaMenu.hasClass('layout-megamenu-active')) {
                $this.layoutMegaMenu.removeClass('fadeInDown').addClass('fadeOutUp');

                setTimeout(function () {
                    $this.layoutMegaMenu.removeClass('layout-megamenu-active fadeOutUp');
                    $(document.body).removeClass('body-megamenu-active');
                }, 250);
            }
            else {
                $(document.body).addClass('body-megamenu-active');
                $this.layoutMegaMenu.addClass('layout-megamenu-active fadeInDown');
            }

            e.preventDefault();
        });

        $this.layoutMegaMenu.off('click.megamenu').on('click.megamenu', function (e) {
            $this.megaMenuClick = true;
        });

        $this.topbarMenuButton.off('click.topbar').on('click.topbar', function (e) {
            //TODO: Move to CSS
            $this.topbarUserMenu.css({ top: '60px', right: '0', left: 'auto' });
            $this.topbarMenuClick = true;

            if ($this.topbarUserMenu.hasClass('usermenu-active')) {
                $this.topbarUserMenu.removeClass('fadeInDown').addClass('fadeOutUp');

                setTimeout(function () {
                    $this.topbarUserMenu.removeClass('usermenu-active fadeOutUp');
                }, 250);
            }
            else {
                $this.topbarUserMenu.addClass('usermenu-active fadeInDown');
            }

            e.preventDefault();
        });

        $this.topbarUserMenu.off('click.topbar').on('click.topbar', function (e) {
            $this.topbarMenuClick = true;
        });

        $this.menulinks.off('click.menu').on('click.menu', function (e) {
            var link = $(this),
                item = link.parent('li'),
                submenu = item.children('ul');

            if (item.hasClass('active-menuitem')) {
                if (submenu.length) {
                    $this.removeMenuitem(item.attr('id'));

                    if($this.isSlimMenu() || $this.isHorizontalMenu()) {
                        item.removeClass('active-menuitem');
                        submenu.hide();
                    }
                    else {
                        submenu.slideUp(400, function() {
                            item.removeClass('active-menuitem');
                        });
                    }
                }

                if(item.parent().is($this.jq)) {
                    $this.menuActive = false;
                }
            }
            else {
                $this.addMenuitem(item.attr('id'));

                if($this.isSlimMenu() || $this.isHorizontalMenu()) {
                    $this.deactivateItems(item.siblings(), false);
                }
                else {
                    $this.deactivateItems(item.siblings(), true);
                    $.cookie('california_menu_scroll_state', link.attr('href') + ',' + $this.menuContainer.scrollTop(), { path: '/' });
                }

                $this.activate(item);

                if(item.parent().is($this.jq)) {
                    $this.menuActive = true;
                }
            }

            if (submenu.length) {
                e.preventDefault();
            }
        });

        this.menu.children('li').off('mouseenter.menuitem').on('mouseenter.menuitem', function(e) {
            if($this.isHorizontalMenu() || $this.isSlimMenu()) {
                var item = $(this);

                if(!item.hasClass('active-menuitem')) {
                    $this.menu.find('.active-menuitem').removeClass('active-menuitem');
                    $this.menu.find('ul:visible').hide();

                    if($this.menuActive) {
                        item.addClass('active-menuitem');
                        item.children('ul').show();
                        
                        if($this.isSlimMenu()) {
                            var userProfile = $this.sidebar.find('.user-profile');
                            userProfile.removeClass('layout-profile-active');
                            userProfile.children('ul').hide();
                        }
                    }
                }
            }
        });
 
        this.sidebar.find('.user-profile').off('mouseenter.sidebar').on('mouseenter.sidebar', function (e) {
            if($this.isSlimMenu()) {
                var item = $(this);

                if(!item.hasClass('layout-profile-active')) {
                    $this.menu.find('.active-menuitem').removeClass('active-menuitem');
                    $this.menu.find('ul:visible').hide();

                    if($this.menuActive) {
                        item.addClass('layout-profile-active');
                        item.children('ul').show();
                    }
                }
            }
        });

        this.sidebarProfileButton.off('click.sidebar').on('click.sidebar', function (e) {
            if ($this.isSlimMenu()) {
                $this.sidebarUserMenuClick = true;
                
                if ($this.sidebarUserContainer.hasClass('layout-profile-active')) {
                    $this.sidebarUserContainer.removeClass('layout-profile-active');
                    $this.sidebarUserMenu.hide();
                }
                else {
                    $this.sidebarUserContainer.addClass('layout-profile-active');
                    $this.sidebarUserMenu.show();
                }
            }
            else if (!$this.isHorizontalMenu()) {
                $this.sidebarUserContainer.toggleClass('layout-profile-active');
                $this.sidebarUserMenu.slideToggle();

                setTimeout(function () {
                    $this.setInlineProfileState($this.sidebarUserMenu.get(0).offsetParent != null);
                }, 500);
            }
            else {
                $this.sidebarUserMenuClick = true;

                if ($this.sidebarUserContainer.hasClass('layout-profile-active')) {
                    $this.sidebarUserMenu.addClass('fadeOutUp');
                    $this.sidebarUserMenu.hide();

                    setTimeout(function () {
                        $this.sidebarUserContainer.removeClass('layout-profile-active');
                        $this.sidebarUserMenu.removeClass('fadeOutUp');
                    }, 150);
                }
                else {
                    $this.sidebarUserContainer.addClass('layout-profile-active');
                    $this.sidebarUserMenu.addClass('fadeInDown');
                    $this.sidebarUserMenu.show();
                }
            }
            e.preventDefault();
        });

        this.usermenuLinks.off('click.profile').on('click.profile', function (e) {
            var link = $(this),
            item = link.parent(),
            submenu = link.next();

            $this.usermenuLinkClick = true;
            $this.sidebarUserMenuClick = true;
            item.siblings('.menuitem-active').removeClass('menuitem-active').children('ul').slideUp();

            if (item.hasClass('menuitem-active')) {
                item.removeClass('menuitem-active');
                submenu.slideUp();
            }
            else {
                item.addClass('menuitem-active');
                submenu.slideDown();
            }

            if (submenu.length) {
                e.preventDefault();
            }
        });

        this.configButton.off('click.configbutton').on('click.configbutton', function(e) {
            $this.configurator.toggleClass('layout-config-active');
            $this.configClicked = true;
        });

        this.configurator.off('click.config').on('click.config', function() {
            $this.configClicked = true;
        });

        $(document.body).off('click.layoutBody').on('click.layoutBody', function () {
            if(($this.isHorizontalMenu() || $this.isSlimMenu()) && !$this.sidebarMenuClick && $this.isDesktop()) {
                $this.menu.find('.active-menuitem').removeClass('active-menuitem');
                $this.menu.find('ul:visible').hide();
                $this.menuActive = false;
            }

            if (!$this.topbarMenuClick && $this.topbarUserMenu.hasClass('usermenu-active')) {
                $this.topbarUserMenu.removeClass('usermenu-active')
            }

            if (!$this.megaMenuClick && $this.layoutMegaMenu.hasClass('layout-megamenu-active')) {
                $this.layoutMegaMenu.removeClass('layout-megamenu-active');
                $(document.body).removeClass('body-megamenu-active');
            }

            if (!$this.rightSidebarClick && $this.rightSidebar.hasClass('layout-right-sidebar-active')) {
                $this.rightSidebar.removeClass('layout-right-sidebar-active')
            }

            if (!$this.sidebarMenuClick && ($this.wrapper.hasClass('layout-wrapper-sidebar-mobile-active') || $this.wrapper.hasClass('layout-wrapper-overlay-sidebar-active'))) {
                $this.wrapper.removeClass('layout-wrapper-sidebar-mobile-active layout-wrapper-overlay-sidebar-active');
            }


            if (!$this.sidebarUserMenuClick && ($this.isHorizontalMenu() || $this.isSlimMenu())) {
                $this.sidebarUserContainer.removeClass('layout-profile-active');
                $this.sidebarUserMenu.hide();
            }

            if (!$this.configClicked && $this.configurator.hasClass('layout-config-active')) {
                $this.configurator.removeClass('layout-config-active');
            }

            $this.megaMenuClick = false;
            $this.topbarMenuClick = false;
            $this.rightSidebarClick = false;
            $this.sidebarMenuClick = false;
            $this.sidebarUserMenuClick = false;
            $this.configClicked = false;
        });

        $(function() {
            $this._initRightSidebar();
        });
    },

    activate: function(item) {
        var submenu = item.children('ul');
        item.addClass('active-menuitem');

        if(submenu.length) {
            if(this.isSlimMenu() || this.isHorizontalMenu())
                submenu.show();
            else
                submenu.slideDown();
        }
    },

    deactivate: function(item) {
        var submenu = item.children('ul');
        item.removeClass('active-menuitem');

        if(submenu.length) {
            submenu.hide();
        }
    },

    deactivateItems: function(items, animate) {
        var $this = this;

        for(var i = 0; i < items.length; i++) {
            var item = items.eq(i),
            submenu = item.children('ul');

            if(submenu.length) {
                if(item.hasClass('active-menuitem')) {
                    var activeSubItems = item.find('.active-menuitem');
                    item.removeClass('active-menuitem');
                    item.find('.ink').remove();

                    if(animate) {
                        submenu.slideUp('normal', function() {
                            $(this).parent().find('.active-menuitem').each(function() {
                                $this.deactivate($(this));
                            });
                        });
                    }
                    else {
                        submenu.hide();
                        item.find('.active-menuitem').each(function() {
                            $this.deactivate($(this));
                        });
                    }

                    $this.removeMenuitem(item.attr('id'));
                    activeSubItems.each(function() {
                        $this.removeMenuitem($(this).attr('id'));
                    });
                }
                else {
                    item.find('.active-menuitem').each(function() {
                        var subItem = $(this);
                        $this.deactivate(subItem);
                        $this.removeMenuitem(subItem.attr('id'));
                    });
                }
            }
            else if(item.hasClass('active-menuitem')) {
                $this.deactivate(item);
                $this.removeMenuitem(item.attr('id'));
            }
        }
    },

    removeMenuitem: function (id) {
        this.expandedMenuitems = $.grep(this.expandedMenuitems, function (value) {
            return value !== id;
        });
        this.saveMenuState();
    },

    addMenuitem: function (id) {
        if ($.inArray(id, this.expandedMenuitems) === -1) {
            this.expandedMenuitems.push(id);
        }
        this.saveMenuState();
    },

    saveMenuState: function() {
        $.cookie('california_expandeditems', this.expandedMenuitems.join(','), {path: '/'});
    },

    clearMenuState: function() {
        $.removeCookie('california_expandeditems', {path: '/'});
    },

    setInlineProfileState: function(expanded) {
        if(expanded)
            $.cookie('california_inlineprofile_expanded', "1", {path: '/'});
        else
            $.removeCookie('california_inlineprofile_expanded', {path: '/'});
    },

    restoreMenuState: function() {
        var $this = this;
        var menucookie = $.cookie('california_expandeditems');
        if (menucookie) {
            this.expandedMenuitems = menucookie.split(',');
            for (var i = 0; i < this.expandedMenuitems.length; i++) {
                var id = this.expandedMenuitems[i];
                if (id) {
                    var menuitem = $("#" + this.expandedMenuitems[i].replace(/:/g, "\\:"));
                    menuitem.addClass('active-menuitem');

                    var submenu = menuitem.children('ul');
                    if(submenu.length) {
                        submenu.show();
                    }
                }
            }

            setTimeout(function() {
                $this.restoreScrollState(menuitem);
            }, 100)
        }

        var inlineProfileCookie = $.cookie('california_inlineprofile_expanded');
        if (inlineProfileCookie) {
            this.sidebarUserMenu.show();
        }
    },

    restoreScrollState: function(menuitem) {
        var scrollState = $.cookie('california_menu_scroll_state');
        if (scrollState) {
            var state = scrollState.split(',');
            if (state[0].startsWith(this.cfg.pathname) || this.isScrolledIntoView(menuitem, state[1])) {
                this.menuContainer.scrollTop(parseInt(state[1], 10));
            }
            else {
                this.scrollIntoView(menuitem.get(0));
                $.removeCookie('california_menu_scroll_state', { path: '/' });
            }
        }
        else if (!this.isScrolledIntoView(menuitem, menuitem.scrollTop())){
            this.scrollIntoView(menuitem.get(0));
        }
    },

    scrollIntoView: function(elem) {
        if (document.documentElement.scrollIntoView) {
            elem.scrollIntoView();
        }
    },

    isScrolledIntoView: function(elem, scrollTop) {
        var viewBottom = parseInt(scrollTop, 10) + this.menuContainer.height();

        var elemTop = elem.position().top;
        var elemBottom = elemTop + elem.height();

        return ((elemBottom <= viewBottom) && (elemTop >= scrollTop));
    },

    enableModal: function() {
        this.modal = this.wrapper.append('<div class="layout-mask"></div>').children('.layout-mask');
    },

    disableModal: function() {
        this.modal.remove();
    },

    isOverlay: function() {
        return this.wrapper.hasClass('layout-wrapper-overlay-sidebar');
    },

    isTablet: function() {
        var width = window.innerWidth;
        return width <= 1024 && width > 640;
    },

    isDesktop: function() {
        return window.innerWidth > 1024;
    },

    isHorizontalMenu: function() {
        return this.wrapper.hasClass('layout-wrapper-horizontal-sidebar') && this.isDesktop();
    },

    isSlimMenu: function() {
        return this.isDesktop() && this.wrapper.hasClass('layout-wrapper-slim-sidebar');
    },

    isMobile: function() {
        return window.innerWidth <= 640;
    },

    _initRightSidebar: function() {
        var $this = this;

        this.rightSidebarButton = $('#right-sidebar-button');
        this.rightSidebar = $('#layout-right-sidebar');

        this.rightSidebarButton.off('click').on('click', function(e) {
            $this.rightSidebar.toggleClass('layout-right-sidebar-active');
            $this.rightSidebarClick = true;
            e.preventDefault();
        });

        this.rightSidebar.off('click').on('click', function (e) {
            $this.rightSidebarClick = true;
            e.preventDefault();
        });
    },

    closeRightSidebarMenu: function() {
        if(this.rightSidebar) {
            this.rightSidebar.removeClass('right-sidebar-active');
        }
    }

});

PrimeFaces.CaliforniaConfigurator = {

    changeMenuMode: function(menuMode) {
        var wrapper = $(document.body).children('.layout-wrapper');
        switch (menuMode) {
            case 'layout-wrapper-overlay-sidebar':
                wrapper.addClass('layout-wrapper-overlay-sidebar').removeClass('layout-wrapper-slim-sidebar layout-wrapper-horizontal-sidebar');
                break;

            case 'layout-wrapper-slim-sidebar':
                wrapper.addClass('layout-wrapper-slim-sidebar').removeClass('layout-wrapper-overlay-sidebar layout-wrapper-horizontal-sidebar');
                break;

            case 'layout-wrapper-horizontal-sidebar':
                wrapper.addClass('layout-wrapper-horizontal-sidebar').removeClass('layout-wrapper-overlay-sidebar layout-wrapper-slim-sidebar');
                break;

            default:
                wrapper.removeClass('layout-wrapper-overlay-sidebar layout-wrapper-slim-sidebar layout-wrapper-horizontal-sidebar');
                break;
        }
    },

    changeMenuColor: function(menuColor) {
        var wrapper = $(document.body).children('.layout-wrapper');
        var sidebar = wrapper.children('.layout-sidebar');
        switch (menuColor) {
            case 'dark':
                sidebar.addClass('layout-sidebar-dark').removeClass('layout-sidebar-gradient');
                break;

            case 'gradient':
                sidebar.addClass('layout-sidebar-dark layout-sidebar-gradient');
                break;

            default:
                sidebar.removeClass('layout-sidebar-dark layout-sidebar-gradient');
                break;
        }
    },

    changeMegaMenuColor: function(megaMenuColor) {
        var megamenu = $('#layout-megamenu');
        switch (megaMenuColor) {
            case 'dark':
                megamenu.addClass('layout-megamenu-dark').removeClass('layout-megamenu-gradient');
                break;

            case 'gradient':
                megamenu.addClass('layout-megamenu-dark layout-megamenu-gradient');
                break;

            default:
                megamenu.removeClass('layout-megamenu-dark layout-megamenu-gradient');
                break;
        }
    },

    changeLayout: function(layoutTheme) {
        var linkElement = $('link[href*="layout-"]');
        var href = linkElement.attr('href');
        var startIndexOf = href.indexOf('layout-') + 7;
        var endIndexOf = href.indexOf('.css');
        var currentColor = href.substring(startIndexOf, endIndexOf);

        this.replaceLink(linkElement, href.replace(currentColor, layoutTheme));
    },

    changeScheme: function(theme) {
        var library = 'primefaces-california';
        var linkElement = $('link[href*="theme.css"]');
        var href = linkElement.attr('href');
        var index = href.indexOf(library) + 1;
        var currentTheme = href.substring(index + library.length);

        this.replaceLink(linkElement, href.replace(currentTheme, theme));
        this.changeLayout(theme);
    },

    beforeResourceChange: function() {
        PrimeFaces.ajax.RESOURCE = null;    //prevent resource append
    },

    replaceLink: function(linkElement, href) {
        PrimeFaces.ajax.RESOURCE = 'javax.faces.Resource';

        var isIE = this.isIE();

        if (isIE) {
            linkElement.attr('href', href);
        }
        else {
            var cloneLinkElement = linkElement.clone(false);

            cloneLinkElement.attr('href', href);
            linkElement.after(cloneLinkElement);

            cloneLinkElement.off('load').on('load', function() {
                linkElement.remove();
            });
        }
    },

    isIE: function() {
        return /(MSIE|Trident\/|Edge\/)/i.test(navigator.userAgent);
    },

    updateInputStyle: function(value) {
        if (value === 'filled')
            $(document.body).addClass('ui-input-filled');
        else
            $(document.body).removeClass('ui-input-filled');
    }
};

/*!
 * jQuery Cookie Plugin v1.4.1
 * https://github.com/carhartl/jquery-cookie
 *
 * Copyright 2006, 2014 Klaus Hartl
 * Released under the MIT license
 */
(function (factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD (Register as an anonymous module)
		define(['jquery'], factory);
	} else if (typeof exports === 'object') {
		// Node/CommonJS
		module.exports = factory(require('jquery'));
	} else {
		// Browser globals
		factory(jQuery);
	}
}(function ($) {

	var pluses = /\+/g;

	function encode(s) {
		return config.raw ? s : encodeURIComponent(s);
	}

	function decode(s) {
		return config.raw ? s : decodeURIComponent(s);
	}

	function stringifyCookieValue(value) {
		return encode(config.json ? JSON.stringify(value) : String(value));
	}

	function parseCookieValue(s) {
		if (s.indexOf('"') === 0) {
			// This is a quoted cookie as according to RFC2068, unescape...
			s = s.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\');
		}

		try {
			// Replace server-side written pluses with spaces.
			// If we can't decode the cookie, ignore it, it's unusable.
			// If we can't parse the cookie, ignore it, it's unusable.
			s = decodeURIComponent(s.replace(pluses, ' '));
			return config.json ? JSON.parse(s) : s;
		} catch(e) {}
	}

	function read(s, converter) {
		var value = config.raw ? s : parseCookieValue(s);
		return $.isFunction(converter) ? converter(value) : value;
	}

	var config = $.cookie = function (key, value, options) {

		// Write

		if (arguments.length > 1 && !$.isFunction(value)) {
			options = $.extend({}, config.defaults, options);

			if (typeof options.expires === 'number') {
				var days = options.expires, t = options.expires = new Date();
				t.setMilliseconds(t.getMilliseconds() + days * 864e+5);
			}

			return (document.cookie = [
				encode(key), '=', stringifyCookieValue(value),
				options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
				options.path    ? '; path=' + options.path : '',
				options.domain  ? '; domain=' + options.domain : '',
				options.secure  ? '; secure' : ''
			].join(''));
		}

		// Read

		var result = key ? undefined : {},
			// To prevent the for loop in the first place assign an empty array
			// in case there are no cookies at all. Also prevents odd result when
			// calling $.cookie().
			cookies = document.cookie ? document.cookie.split('; ') : [],
			i = 0,
			l = cookies.length;

		for (; i < l; i++) {
			var parts = cookies[i].split('='),
				name = decode(parts.shift()),
				cookie = parts.join('=');

			if (key === name) {
				// If second argument (value) is a function it's a converter...
				result = read(cookie, value);
				break;
			}

			// Prevent storing a cookie that we couldn't decode.
			if (!key && (cookie = read(cookie)) !== undefined) {
				result[name] = cookie;
			}
		}

		return result;
	};

	config.defaults = {};

	$.removeCookie = function (key, options) {
		// Must not alter options, thus extending a fresh object...
		$.cookie(key, '', $.extend({}, options, { expires: -1 }));
		return !$.cookie(key);
	};

}));

/* Issue #924 is fixed for 5.3+ and 6.0. (compatibility with 5.3) */
if(window['PrimeFaces'] && window['PrimeFaces'].widget.Dialog) {
    PrimeFaces.widget.Dialog = PrimeFaces.widget.Dialog.extend({

        enableModality: function() {
            this._super();
            $(document.body).children(this.jqId + '_modal').addClass('ui-dialog-mask');
        },

        syncWindowResize: function() {}
    });
} 

if (PrimeFaces.widget.InputSwitch) {
    PrimeFaces.widget.InputSwitch = PrimeFaces.widget.InputSwitch.extend({

        init: function (cfg) {
            this._super(cfg);

            if (this.input.prop('checked')) {
                this.jq.addClass('ui-inputswitch-checked');
            }
        },

        toggle: function () {
            var $this = this;

            if(this.input.prop('checked')) {
                 this.uncheck(); 
                 setTimeout(function() {
                    $this.jq.removeClass('ui-inputswitch-checked');
                 }, 100);
             }
             else {
                 this.check();
                 setTimeout(function() {
                    $this.jq.addClass('ui-inputswitch-checked');
                 }, 100);
             }
        }
    });
}

/* Issue #2131 */
if(window['PrimeFaces'] && window['PrimeFaces'].widget.Schedule && isLtPF8Version()) {
    PrimeFaces.widget.Schedule = PrimeFaces.widget.Schedule.extend({
        
        setupEventSource: function() {
            var $this = this,
            offset = moment().utcOffset()*60000;

            this.cfg.events = function(start, end, timezone, callback) {
                var options = {
                    source: $this.id,
                    process: $this.id,
                    update: $this.id,
                    formId: $this.cfg.formId,
                    params: [
                        {name: $this.id + '_start', value: start.valueOf() + offset},
                        {name: $this.id + '_end', value: end.valueOf() + offset}
                    ],
                    onsuccess: function(responseXML, status, xhr) {
                        PrimeFaces.ajax.Response.handle(responseXML, status, xhr, {
                                widget: $this,
                                handle: function(content) {
                                    callback($.parseJSON(content).events);
                                }
                            });

                        return true;
                    }
                };

                PrimeFaces.ajax.Request.handle(options);
            }
        }
    });
}

function isLtPF8Version() {
    var version = window['PrimeFaces'].VERSION;
    if (!version) {
        return true;
    }

    return parseInt(version.split('.')[0], 10) < 8;
}