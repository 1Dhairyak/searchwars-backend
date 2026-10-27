-- Items are now ranked by real monthly Wikipedia page views (WikipediaViewsJob),
-- not estimated Google search volume.
alter table items rename column search_volume to monthly_views;
alter table items add column views_refreshed_at timestamp(6);
